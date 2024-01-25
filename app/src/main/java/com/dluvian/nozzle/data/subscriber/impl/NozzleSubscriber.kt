package com.dluvian.nozzle.data.subscriber.impl

import android.util.Log
import com.dluvian.nozzle.data.SHORT_WAIT_TIME
import com.dluvian.nozzle.data.annotatedContent.IAnnotatedContentHandler
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.nostr.utils.KeyUtils
import com.dluvian.nozzle.data.nostr.utils.MentionUtils
import com.dluvian.nozzle.data.provider.IAccountProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.AppDatabase
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.subscriber.ISubscriptionQueue
import com.dluvian.nozzle.data.utils.getMaxRelaysAndAddIfTooSmall
import com.dluvian.nozzle.data.utils.takeRandom80percent
import com.dluvian.nozzle.model.FeedInfo
import com.dluvian.nozzle.model.IdAndRelays
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile
import com.dluvian.nozzle.model.nostr.ReplyTo
import kotlinx.coroutines.delay

private const val TAG = "NozzleSubscriber"

class NozzleSubscriber(
    private val subQueue: ISubscriptionQueue,
    private val relayProvider: IRelayProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val accountProvider: IAccountProvider,
    private val annotatedContentHandler: IAnnotatedContentHandler,
    private val idCache: IIdCache,
    private val database: AppDatabase,
) : INozzleSubscriber {

    override suspend fun subscribePersonalProfiles() {
        Log.i(TAG, "Subscribe personal profiles")
        val accountPubkeys = accountProvider.listAccounts().map { it.pubkey }
        submitFullProfiles(pubkeys = accountPubkeys)
    }

    override suspend fun subscribeUnknownContacts() {
        Log.i(TAG, "Subscribe unknown contacts")
        val pubkeys = database.contactDao().listContactPubkeysWithMissingProfile()
        submitFullProfiles(pubkeys = pubkeys)
    }

    override suspend fun subscribeUnknowns(notes: Collection<PostWithMeta>) {
        Log.i(TAG, "Subscribe unknowns")
        if (notes.isEmpty()) return

        submitSimpleUnknowns(notes = notes)
        submitComplexUnknowns(notes = notes)
    }

    override suspend fun subscribeFullProfile(profileId: String) {
        Log.i(TAG, "Subscribe full profile of $profileId")
        val nostrId = EncodingUtils.profileIdToNostrId(profileId = profileId)
        val hex = nostrId?.hex ?: profileId
        val recommendedRelays = nostrId?.recommendedRelays.orEmpty()
            .ifEmpty { relayProvider.getWriteRelaysOfPubkey(pubkey = hex) }
        val relays = getMaxRelaysAndAddIfTooSmall(
            from = recommendedRelays,
            prefer = relayProvider.getReadRelays(limit = false)

        )
        subQueue.submitFullProfile(pubkey = hex, relays = relays)
        subQueue.processNow()
    }

    override suspend fun subscribeSimpleProfiles(pubkeys: Collection<String>) {
        Log.i(TAG, "Subscribe ${pubkeys.size} simple profiles")
        if (pubkeys.isEmpty()) return

        val pubkeysWithNoWriteRelays = relayProvider.getWriteRelaysByPubkeys(pubkeys = pubkeys)
            .filter { (_, writeRelays) -> writeRelays.isEmpty() }
            .map { (pubkey, _) -> pubkey }
        subQueue.submitNip65s(pubkeys = pubkeysWithNoWriteRelays, relays = null)
        subQueue.processNow()

        delay(SHORT_WAIT_TIME)

        val myReadRelays = relayProvider.getReadRelays(limit = true)
        relayProvider.getWriteRelaysByPubkeys(pubkeys = pubkeys)
            .forEach { (pubkey, writeRelays) ->
                val relays = getMaxRelaysAndAddIfTooSmall(from = writeRelays, prefer = myReadRelays)
                subQueue.submitProfile(pubkey = pubkey, relays = relays)
            }
        subQueue.processNow()
    }

    override fun subscribeToFeed(
        pubkeysByRelay: Map<Relay, Set<Pubkey>?>,
        hashtag: String?,
        limit: Int,
        until: Long,
    ) {
        Log.i(TAG, "Subscribe feed posts")
        pubkeysByRelay.forEach { (relay, pubkeys) ->
            subQueue.submitFeed(
                until = until,
                limit = limit,
                hashtag = hashtag,
                authors = pubkeys?.toList(),
                relays = listOf(relay)
            )
        }
        subQueue.processNow()
    }

    override fun subscribeToInbox(
        limit: Int,
        relays: Collection<String>,
        until: Long,
    ) {
        Log.i(TAG, "Subscribe inbox")
        if (relays.isEmpty() || limit <= 0) return

        subQueue.submitInbox(
            until = until,
            limit = limit,
            mentionedPubkey = pubkeyProvider.getActivePubkey(),
            relays = relays
        )
        subQueue.processNow()
    }

    override fun subscribeToLikes(limit: Int, until: Long) {
        Log.i(TAG, "Subscribe likes")
        if (limit <= 0) return

        subQueue.submitLikes(
            limit = limit,
            until = until,
            author = pubkeyProvider.getActivePubkey(),
            relays = relayProvider.getWriteRelays()
        )
        subQueue.processNow()
    }

    override suspend fun subscribeFeedInfo(posts: List<PostEntity>): FeedInfo {
        Log.i(TAG, "Subscribe feed info")
        if (posts.isEmpty()) return FeedInfo.createEmpty()

        val postIds = posts.map { it.id }
        val authorPubkeys = posts.map { it.pubkey }.distinct()
        val contents = posts.map { it.content }
        val mentionedProfiles = MentionUtils.extractNprofilesAndNpubs(contents = contents)
        val mentionedPubkeys = mentionedProfiles.map { it.pubkey }
        val mentionedPosts = MentionUtils.extractNeventsAndNoteIds(contents = contents)
        val mentionedPostIds = mentionedPosts.map { it.eventId }
        val replyTos = posts.mapNotNull {
            it.replyToId?.let { id ->
                ReplyTo(
                    replyTo = id,
                    relayUrl = it.replyRelayHint
                )
            }
        }
        val unknownParentAuthors = getUnknownParentAuthors(
            replyTos = replyTos,
            mentionedPostIds = mentionedPostIds
        )
        val allPubkeys = authorPubkeys + mentionedPubkeys + unknownParentAuthors
        val postIdsByPubkey = authorPubkeys.associateWith { pubkey ->
            posts.filter { it.pubkey == pubkey }.map { it.id }
        }
        getNip65PubkeysToSub(pubkeys = allPubkeys, nprofiles = mentionedProfiles)
            .forEach { (relay, pubkeys) ->
                subQueue.submitNip65s(pubkeys = pubkeys, relays = listOf(relay))
            }

        getProfilePubkeysToSub(authorPubkeys = allPubkeys, mentionedProfiles = mentionedProfiles)
            .forEach { (relay, pubkeys) ->
                subQueue.submitProfiles(pubkeys = pubkeys, relays = listOf(relay))
            }
        getContactListPubkeysToSub(
            authorPubkeys = authorPubkeys,
            mentionedProfiles = mentionedProfiles
        ).forEach { (relay, pubkeys) ->
            subQueue.submitContactLists(pubkeys = pubkeys, relays = listOf(relay))
        }
        getPostIdsToSub(replyTos = replyTos, mentionedPosts = mentionedPosts)
            .forEach { (relay, noteIds) ->
                subQueue.submitNoteIds(noteIds = noteIds, relays = listOf(relay))
            }
        getPostIdsToSubReplies(postIdsByPubkey = postIdsByPubkey).forEach { (relay, noteIds) ->
            subQueue.submitReplies(parentIds = noteIds, relays = listOf(relay))
        }
        getReactionPostIdsToSub(postIds = postIds).forEach { (relay, noteIds) ->
            subQueue.submitLikes(
                noteIds = noteIds,
                author = pubkeyProvider.getActivePubkey(),
                relays = listOf(relay)
            )
        }

        return FeedInfo(
            postIds = postIds,
            authorPubkeys = authorPubkeys,
            mentionedPubkeys = mentionedPubkeys,
            mentionedPostIds = mentionedPostIds,
        )
    }

    override suspend fun subscribeThreadPost(postId: String) {
        Log.i(TAG, "Subscribe thread post")

        val nostrId = EncodingUtils.postIdToNostrId(postId)
        val hex = nostrId?.hex ?: postId

        if (!KeyUtils.isValidPubkey(hex)) {
            Log.w(TAG, "Tried to sub invalid pubkey $hex")
            return
        }

        val postIds = listOf(hex)
        val existingIds = database.postDao().filterExistingIds(postIds = postIds)
        if (existingIds.contains(hex)) return

        val relays = getMaxRelaysAndAddIfTooSmall(
            from = nostrId?.recommendedRelays.orEmpty(),
            prefer = relayProvider.getReadRelays(limit = false)
        )
        subQueue.submitNoteId(noteId = hex, relays = relays)
        subQueue.processNow()
    }

    override suspend fun subscribeParentPost(noteId: NoteId, relayHint: Relay?) {
        Log.i(TAG, "Subscribe parent post $noteId in $relayHint")

        val relays = getMaxRelaysAndAddIfTooSmall(
            from = if (relayHint.isNullOrEmpty()) listOf() else listOf(relayHint),
            prefer = relayProvider.getReadRelays(limit = false)
        )
        subQueue.submitNoteId(noteId = noteId, relays = relays)
        subQueue.processNow()
    }

    override suspend fun subscribeNip65(pubkeys: Set<Pubkey>) {
        Log.i(TAG, "Subscribe nip65 of ${pubkeys.size} pubkeys")
        if (pubkeys.isEmpty()) return

        val filteredPubkeys = pubkeys.minus(idCache.getNip65Authors())
        if (filteredPubkeys.isEmpty()) return

        val pubkeysInDb = database.nip65Dao().filterPubkeysWithNip65(pubkeys = filteredPubkeys)
        val toSub = filteredPubkeys.minus(pubkeysInDb.takeRandom80percent().toSet())
        if (toSub.isEmpty()) return

        subQueue.submitNip65s(pubkeys = toSub.toList(), relays = null)
        subQueue.processNow()
    }

    override fun subscribeToNotes(noteIds: Collection<NoteId>) {
        if (noteIds.isEmpty()) return

        val distinctIds = noteIds.distinct()
        Log.i(TAG, "Subscribe ${distinctIds.size} notes")

        val myReadRelays = relayProvider.getReadRelays(limit = true)
        noteIds.forEach { noteId -> subQueue.submitNoteId(noteId = noteId, relays = myReadRelays) }
        subQueue.processNow()
    }

    private suspend fun getNip65PubkeysToSub(
        pubkeys: List<Pubkey>,
        nprofiles: List<Nprofile>
    ): Map<Relay, List<Pubkey>> {
        val filtered = handlePubkeyFiltering(
            pubkeys = pubkeys,
            profiles = nprofiles,
            getSessionPubkeys = { idCache.getNip65Authors() },
            getDbPubkeys = { filteredPubkeys ->
                database.nip65Dao().filterPubkeysWithNip65(pubkeys = filteredPubkeys)
            }
        )
        if (filtered.isEmpty()) return emptyMap()

        Log.i(TAG, "Return ${filtered.values.first().size} pubkeys to subscribe nip65s")
        return filtered.toMutableMap()
            .addSpecialRelayMapping(idAndRelays = getIdAndRelays(nprofiles))
    }

    private suspend fun getProfilePubkeysToSub(
        authorPubkeys: List<Pubkey>,
        mentionedProfiles: List<Nprofile>
    ): Map<Relay, List<Pubkey>> {
        val filtered = handlePubkeyFiltering(
            pubkeys = authorPubkeys,
            profiles = mentionedProfiles,
            getSessionPubkeys = { idCache.getPubkeys() },
            getDbPubkeys = { filteredPubkeys ->
                database.profileDao().filterExistingPubkeys(pubkeys = filteredPubkeys)
            }
        )
        if (filtered.isEmpty()) return emptyMap()

        Log.i(TAG, "Return ${filtered.values.first().size} pubkeys to subscribe profiles")
        return filtered.toMutableMap()
            .addSpecialRelayMapping(idAndRelays = getIdAndRelays(mentionedProfiles))
    }

    private suspend fun getContactListPubkeysToSub(
        authorPubkeys: List<Pubkey>,
        mentionedProfiles: List<Nprofile>
    ): Map<Relay, List<Pubkey>> {
        val filtered = handlePubkeyFiltering(
            pubkeys = authorPubkeys,
            profiles = mentionedProfiles,
            getSessionPubkeys = { idCache.getContactListAuthors() },
            getDbPubkeys = { filteredPubkeys ->
                database.contactDao().filterFriendsWithList(contactPubkeys = filteredPubkeys)
            }
        )
        if (filtered.isEmpty()) return emptyMap()

        Log.i(TAG, "Return ${filtered.values.first().size} pubkeys to subscribe contact lists")

        return filtered.toMutableMap()
            .addSpecialRelayMapping(idAndRelays = getIdAndRelays(mentionedProfiles))
    }

    private suspend fun getPostIdsToSub(
        replyTos: List<ReplyTo>,
        mentionedPosts: List<Nevent>
    ): Map<Relay, List<NoteId>> {
        if (replyTos.isEmpty() && mentionedPosts.isEmpty()) return emptyMap()

        val allPostIds = replyTos.map { it.replyTo } + mentionedPosts.map { it.eventId }

        val filtered = handleDbFiltering(
            ids = allPostIds,
            getDbIds = { ids -> database.postDao().filterExistingIds(ids) }
        )
        if (filtered.isEmpty()) return emptyMap()

        Log.i(TAG, "Return ${filtered.values.first().size} postIds to subscribe to")
        val idAndRelays = getIdAndRelays(replyTos = replyTos, mentionedPosts = mentionedPosts)
        return filtered.toMutableMap().addSpecialRelayMapping(idAndRelays = idAndRelays)
    }

    private suspend fun getPostIdsToSubReplies(
        postIdsByPubkey: Map<Pubkey, List<NoteId>>
    ): Map<Relay, List<NoteId>> {
        if (postIdsByPubkey.isEmpty()) return emptyMap()

        val allPostIds = postIdsByPubkey.flatMap { (_, postIds) -> postIds }
            .distinct()
            .toMutableList()
        val result = relayProvider.getReadRelays(limit = true)
            .associateWith { _ -> allPostIds }
            .toMutableMap()

        // Get from read relays bc that's where people post replies to
        val readRelaysByPubkey = relayProvider.getReadRelaysOfPubkeys(
            pubkeys = postIdsByPubkey.map { (pubkey, _) -> pubkey }
        )
        for ((pubkey, readRelays) in readRelaysByPubkey) {
            getMaxRelaysAndAddIfTooSmall(from = readRelays, prefer = result.keys).forEach { relay ->
                val postIdsToAdd = postIdsByPubkey[pubkey].orEmpty().toMutableList()
                val present = result.putIfAbsent(relay, postIdsToAdd)
                present?.addAll(postIdsToAdd)
            }
        }

        return result
    }

    private suspend fun getReactionPostIdsToSub(postIds: List<NoteId>): Map<Relay, List<NoteId>> {
        if (postIds.isEmpty()) return emptyMap()

        val alreadyLiked = database.reactionDao().filterLikedPostIds(
            postIds = postIds,
            pubkey = pubkeyProvider.getActivePubkey()
        )
        val toSub = postIds.minus(alreadyLiked.toSet())
        if (toSub.isEmpty()) return emptyMap()

        Log.i(TAG, "Return ${toSub.size} postIds to sub reactions")
        return relayProvider.getReadRelays(limit = true).associateWith { _ -> toSub }
    }

    private suspend fun getUnknownParentAuthors(
        replyTos: List<ReplyTo>,
        mentionedPostIds: List<String>
    ): List<String> {
        if (replyTos.isEmpty() && mentionedPostIds.isEmpty()) return emptyList()

        val postIds = replyTos.map { it.replyTo } + mentionedPostIds
        val unknown = database.postDao().getUnknownAuthors(postIds = postIds)

        Log.i(TAG, "Return ${unknown.size} unknown author pubkeys")
        return unknown
    }

    private fun getIdAndRelays(mentionedProfiles: List<Nprofile>): List<IdAndRelays> {
        return mentionedProfiles.map { IdAndRelays(id = it.pubkey, relays = it.relays) }
    }

    private fun getIdAndRelays(
        replyTos: List<ReplyTo>,
        mentionedPosts: List<Nevent>
    ): List<IdAndRelays> {
        val replyToIdAndRelays = replyTos.mapNotNull { replyTo ->
            replyTo.relayUrl?.let { relay ->
                IdAndRelays(id = replyTo.replyTo, relays = listOf(relay))
            }
        }
        val mentionedIdAndRelays = mentionedPosts.map {
            IdAndRelays(id = it.eventId, relays = it.relays)
        }
        return replyToIdAndRelays + mentionedIdAndRelays
    }

    private suspend fun handlePubkeyFiltering(
        pubkeys: List<Pubkey>,
        profiles: List<Nprofile>,
        getSessionPubkeys: () -> Set<Pubkey>,
        getDbPubkeys: suspend (List<Pubkey>) -> List<Pubkey>
    ): Map<Relay, MutableList<Pubkey>> {
        if (pubkeys.isEmpty() && profiles.isEmpty()) return emptyMap()

        val allPubkeys = pubkeys + profiles.map { it.pubkey }

        val receivedInSessionPubkeys = getSessionPubkeys()
        val filteredPubkeys = allPubkeys.minus(receivedInSessionPubkeys)

        return handleDbFiltering(
            ids = filteredPubkeys,
            getDbIds = { getDbPubkeys(filteredPubkeys).takeRandom80percent() }
        )
    }

    private suspend fun handleDbFiltering(
        ids: List<String>,
        getDbIds: suspend (List<String>) -> List<String>
    ): Map<Relay, MutableList<Pubkey>> {
        if (ids.isEmpty()) return emptyMap()

        val existingIds = getDbIds(ids).toSet()
        val filtered = ids.minus(existingIds).distinct()
        if (filtered.isEmpty()) return emptyMap()

        return relayProvider.getReadRelays(limit = true).associateWith { _ ->
            filtered.toMutableList()
        }
    }

    private fun MutableMap<Relay, MutableList<String>>.addSpecialRelayMapping(
        idAndRelays: Collection<IdAndRelays>
    ): MutableMap<Relay, MutableList<String>> {
        val toSub = this.values.flatten().toSet()
        if (toSub.isEmpty()) return this

        val subInSpecialRelay = idAndRelays.filter {
            toSub.contains(it.id) && it.relays.any { relay -> !this.keys.contains(relay) }
        }
        if (subInSpecialRelay.isEmpty()) return this

        Log.i(TAG, "Handle ${subInSpecialRelay.size} ids with special relays")
        subInSpecialRelay.forEach { entry ->
            entry.relays.forEach { relay ->
                val pubkeys = this.putIfAbsent(relay, mutableListOf(entry.id))
                pubkeys?.add(entry.id)
            }
        }

        return this
    }

    private suspend fun submitFullProfiles(pubkeys: Collection<Pubkey>) {
        if (pubkeys.isEmpty()) return
        val myReadRelays = relayProvider.getReadRelays(limit = false)
        val writeRelays = relayProvider.getWriteRelaysByPubkeys(pubkeys = pubkeys)
        writeRelays.forEach { (pubkey, relays) ->
            subQueue.submitFullProfile(
                pubkey = pubkey,
                relays = getMaxRelaysAndAddIfTooSmall(from = relays, prefer = myReadRelays)
            )
        }
    }

    private suspend fun submitFullNprofiles(nprofiles: Collection<Nprofile>) {
        if (nprofiles.isEmpty()) return
        val myReadRelays = relayProvider.getReadRelays(limit = false)
        val writeRelays =
            relayProvider.getWriteRelaysByPubkeys(pubkeys = nprofiles.map { it.pubkey })
        nprofiles.forEach { nprofile ->
            subQueue.submitFullProfile(
                pubkey = nprofile.pubkey,
                relays = getMaxRelaysAndAddIfTooSmall(
                    from = (nprofile.relays + writeRelays[nprofile.pubkey].orEmpty()).distinct(),
                    prefer = myReadRelays
                )
            )
        }
        writeRelays.forEach { (pubkey, relays) ->
            subQueue.submitFullProfile(
                pubkey = pubkey,
                relays = getMaxRelaysAndAddIfTooSmall(from = relays, prefer = myReadRelays)
            )
        }
    }

    private suspend fun submitSimpleUnknowns(notes: Collection<PostWithMeta>) {
        if (notes.isEmpty()) return

        val unknownAuthors = notes
            .filter { it.hasUnknownAuthor }
            .map { it.pubkey }
        val unknownReplyParentAuthors = notes
            .filter { it.replyToName.isNullOrEmpty() && it.entity.replyRelayHint == null }
            .mapNotNull { it.replyToPubkey }
        val unknownAuthorsOfMentionedPost = notes
            .flatMap { it.annotatedMentionedPosts }
            .filter { it.mentionedPost.name.isNullOrEmpty() }
            .mapNotNull { it.mentionedPost.pubkey }
        val allPubkeys = listOf(
            unknownAuthors,
            unknownReplyParentAuthors,
            unknownAuthorsOfMentionedPost
        )
            .flatten()
            .toSet()
        submitFullProfiles(pubkeys = allPubkeys)

        val mentionedProfilesOfMentionedPost = notes
            .flatMap { it.annotatedMentionedPosts }
            .flatMap { annotatedContentHandler.extractNprofiles(it.annotatedContent) }
        submitFullNprofiles(nprofiles = mentionedProfilesOfMentionedPost)

        val unknownMentionedNoteIds = notes
            .flatMap { it.annotatedMentionedPosts }
            .filter { it.mentionedPost.content == null }
            .map { it.mentionedPost.id }
        subQueue.submitNoteIds(noteIds = unknownMentionedNoteIds, relays = null)

        val myReadRelays = relayProvider.getReadRelays(limit = false)
        val unknownParentNotes = notes.filter {
            it.entity.replyToId != null && it.replyToPubkey == null
        }
        unknownParentNotes.forEach {
            val relays = if (it.entity.replyRelayHint.isNullOrEmpty()) null
            else getMaxRelaysAndAddIfTooSmall(
                from = listOf(it.entity.replyRelayHint),
                prefer = myReadRelays
            )
            it.entity.replyToId?.let { replyToId ->
                subQueue.submitNoteId(noteId = replyToId, relays = relays)
            }
        }
    }

    private suspend fun submitComplexUnknowns(notes: Collection<PostWithMeta>) {
        if (notes.isEmpty()) return

        val myReadRelays = relayProvider.getReadRelays(limit = false)

        val allAnnotations = (notes.map { it.annotatedContent } +
                notes.flatMap { it.annotatedMentionedPosts }.map { it.annotatedContent }).toSet()

        val nprofiles = allAnnotations.flatMap { annotatedContentHandler.extractNprofiles(it) }
        val allPubkeys = nprofiles.map { it.pubkey }.toSet()
        val existingPubkeys = database.profileDao().filterExistingPubkeys(pubkeys = allPubkeys)
        val unknownPubkeys = allPubkeys - existingPubkeys.toSet()
        val unknownNprofiles = nprofiles.filter { unknownPubkeys.contains(it.pubkey) }
        unknownNprofiles.forEach {
            val relays = getMaxRelaysAndAddIfTooSmall(from = it.relays, prefer = myReadRelays)
            subQueue.submitFullProfile(pubkey = it.pubkey, relays = relays)
        }

        val nevents = allAnnotations.flatMap { annotatedContentHandler.extractNevents(it) }
        val allNoteIds = nevents.map { it.eventId }.toSet()
        val existingNoteIds = database.postDao().filterExistingIds(postIds = allNoteIds)
        val unknownNoteIds = allNoteIds - existingNoteIds.toSet()
        val unknownNevents = nevents.filter { unknownNoteIds.contains(it.eventId) }
        unknownNevents.forEach {
            val relays = getMaxRelaysAndAddIfTooSmall(from = it.relays, prefer = myReadRelays)
            subQueue.submitNoteId(noteId = it.eventId, relays = relays)
        }
    }
}
