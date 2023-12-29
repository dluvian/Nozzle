package com.dluvian.nozzle.data.subscriber.impl

import android.util.Log
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.annotatedContent.IAnnotatedContentHandler
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.INostrSubscriber
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
import com.dluvian.nozzle.data.utils.getMaxRelays
import com.dluvian.nozzle.data.utils.getMaxRelaysAndAddIfTooSmall
import com.dluvian.nozzle.data.utils.takeRandom80percent
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.FeedInfo
import com.dluvian.nozzle.model.IdAndRelays
import com.dluvian.nozzle.model.MultipleRelays
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.RelaySelection
import com.dluvian.nozzle.model.UserSpecific
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile
import com.dluvian.nozzle.model.nostr.ReplyTo
import kotlinx.coroutines.delay
import java.util.Collections

private const val TAG = "NozzleSubscriber"

class NozzleSubscriber(
    private val nostrSubscriber: INostrSubscriber,
    private val subQueue: ISubscriptionQueue,
    private val relayProvider: IRelayProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val accountProvider: IAccountProvider,
    private val annotatedContentHandler: IAnnotatedContentHandler,
    private val idCache: IIdCache,
    private val database: AppDatabase,
) : INozzleSubscriber {
    private val feedInfoSubs = Collections.synchronizedList(mutableListOf<String>())
    private val nip65Subs = Collections.synchronizedList(mutableListOf<String>())
    private val postSubs = Collections.synchronizedList(mutableListOf<String>())
    private val threadPostSubs = Collections.synchronizedList(mutableListOf<String>())
    private val parentPostSubs = Collections.synchronizedList(mutableListOf<String>())

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
            prefer = relayProvider.getReadRelays()
        )
        subQueue.submitFullProfile(pubkey = hex, relays = relays)
        subQueue.processNow()
    }

    override suspend fun subscribeSimpleProfiles(pubkeys: Collection<String>) {
        Log.i(TAG, "Subscribe ${pubkeys.size} simple profiles")
        if (pubkeys.isEmpty()) return

        relayProvider.getWriteRelaysByPubkeys(pubkeys = pubkeys)
            .filter { (_, writeRelays) -> writeRelays.isEmpty() }
            .map { (pubkey, _) -> pubkey }
            .forEach { pubkey -> subQueue.submitNip65(pubkey = pubkey, relays = null) }
        subQueue.processNow()

        delay(WAIT_TIME)

        val myReadRelays = relayProvider.getReadRelays()
        relayProvider.getWriteRelaysByPubkeys(pubkeys = pubkeys)
            .forEach { (pubkey, writeRelays) ->
                val relays = getMaxRelaysAndAddIfTooSmall(from = writeRelays, prefer = myReadRelays)
                subQueue.submitProfile(pubkey = pubkey, relays = relays)
            }
    }

    override fun subscribeToFeed(
        limit: Int,
        authors: List<Pubkey>?,
        relaySelection: RelaySelection,
        until: Long,
    ) {
        Log.i(TAG, "Subscribe feed posts")
        if (authors != null && authors.isEmpty() || limit <= 0) return

        when (relaySelection) {
            is AllRelays, is MultipleRelays -> {
                subQueue.submitFeed(
                    until = until,
                    limit = limit,
                    authors = authors,
                    relays = relaySelection.selectedRelays
                )
            }

            is UserSpecific -> {
                relaySelection.pubkeysPerRelay.forEach { (relay, pubkeys) ->
                    subQueue.submitFeed(
                        until = until,
                        limit = limit,
                        authors = pubkeys,
                        relays = listOf(relay)
                    )
                }
            }
        }
        subQueue.processNow()
    }

    override fun subscribeToHashtag(
        limit: Int,
        hashtag: String,
        relays: Collection<Relay>,
        until: Long
    ) {
        Log.i(TAG, "Subscribe feed posts")
        if (hashtag.isBlank() || limit <= 0 || relays.isEmpty()) return
        subQueue.submitHashtag(
            until = until,
            limit = limit,
            hashtag = hashtag,
            relays = relays
        )
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

        val subIds = nostrSubscriber.subscribeFeedInfo(
            nip65PubkeysByRelay = getNip65PubkeysToSub(
                pubkeys = allPubkeys,
                nprofiles = mentionedProfiles
            ),
            profilePubkeysByRelay = getProfilePubkeysToSub(
                authorPubkeys = allPubkeys,
                mentionedProfiles = mentionedProfiles
            ),
            contactListPubkeysByRelay = getContactListPubkeysToSub(
                authorPubkeys = authorPubkeys,
                mentionedProfiles = mentionedProfiles
            ),
            postIdsByRelay = getPostIdsToSub(replyTos = replyTos, mentionedPosts = mentionedPosts),
            repliesByRelay = getPostIdsToSubReplies(postIdsByPubkey = postIdsByPubkey),
            reactionPostIdsByRelay = getReactionPostIdsToSub(postIds = postIds),
            reactorPubkey = pubkeyProvider.getActivePubkey()
        )
        feedInfoSubs.unsubThenAddAll(subIds)

        return FeedInfo(
            postIds = postIds,
            authorPubkeys = authorPubkeys,
            mentionedPubkeys = mentionedPubkeys,
            mentionedPostIds = mentionedPostIds,
        )
    }

    override suspend fun subscribeThreadPost(postId: String) {
        Log.i(TAG, "Subscribe thread post")
        parentPostSubs.unsubThenAddAll(emptyList())

        val nostrId = EncodingUtils.postIdToNostrId(postId)
        val hex = nostrId?.hex ?: postId

        if (!KeyUtils.isValidPubkey(hex)) {
            Log.w(TAG, "Tried to sub invalid pubkey $hex")
            return
        }

        val postIds = listOf(hex)
        val existingIds = database.postDao().filterExistingIds(postIds = postIds)
        if (existingIds.contains(hex)) return

        val relays = nostrId?.recommendedRelays.orEmpty() + relayProvider.getReadRelays()
        val subIds = nostrSubscriber.subscribePosts(postIds = postIds, relays = relays)

        threadPostSubs.unsubThenAddAll(subIds)
    }

    override suspend fun subscribeParentPost(postId: String, relayHint: String?) {
        Log.i(TAG, "Subscribe parent post $postId in $relayHint")

        val relays = relayProvider.getReadRelays().toMutableList()
        if (relayHint?.isNotEmpty() == true) relays.add(relayHint)

        val subIds = nostrSubscriber.subscribePosts(postIds = listOf(postId), relays = relays)
        parentPostSubs.addAll(subIds)
    }

    override suspend fun subscribeNip65(pubkeys: Set<String>) {
        Log.i(TAG, "Subscribe nip65 of ${pubkeys.size} pubkeys")
        if (pubkeys.isEmpty()) return

        val filteredPubkeys = pubkeys.minus(idCache.getNip65Authors())
        if (filteredPubkeys.isEmpty()) return

        val pubkeysInDb = database.nip65Dao().filterPubkeysWithNip65(pubkeys = filteredPubkeys)
        val toSub = filteredPubkeys.minus(pubkeysInDb.takeRandom80percent().toSet())
        if (toSub.isEmpty()) return

        val subIds = nostrSubscriber.subscribeNip65(pubkeys = toSub)
        nip65Subs.unsubThenAddAll(subIds)
    }

    override fun subscribeToPosts(postIds: Collection<String>) {
        if (postIds.isEmpty()) return

        val distinctIds = postIds.distinct()
        Log.i(TAG, "Subscribe ${distinctIds.size} posts")

        val subIds = nostrSubscriber.subscribePosts(
            postIds = distinctIds,
            relays = relayProvider.getReadRelays()
        )
        postSubs.unsubThenAddAll(subIds)
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
    ): Map<Relay, List<String>> {
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

    private suspend fun getPostIdsToSubReplies(postIdsByPubkey: Map<Pubkey, List<NoteId>>): Map<Relay, List<NoteId>> {
        if (postIdsByPubkey.isEmpty()) return emptyMap()

        val allPostIds = postIdsByPubkey.flatMap { (_, postIds) -> postIds }
            .distinct()
            .toMutableList()
        val result = relayProvider.getReadRelays()
            .associateWith { _ -> allPostIds }
            .toMutableMap()

        // Get from read relays bc that's where people post replies to
        val readRelaysByPubkey = relayProvider.getReadRelaysOfPubkeys(
            pubkeys = postIdsByPubkey.map { (pubkey, _) -> pubkey }
        )
        for ((pubkey, readRelays) in readRelaysByPubkey) {
            getMaxRelays(from = readRelays, prefer = result.keys).forEach { relay ->
                val postIdsToAdd = postIdsByPubkey[pubkey].orEmpty().toMutableList()
                val present = result.putIfAbsent(relay, postIdsToAdd)
                present?.addAll(postIdsToAdd)
            }
        }

        return result
    }

    private suspend fun getReactionPostIdsToSub(postIds: List<String>): Map<Relay, List<String>> {
        if (postIds.isEmpty()) return emptyMap()

        val alreadyLiked = database.reactionDao().filterLikedPostIds(
            postIds = postIds,
            pubkey = pubkeyProvider.getActivePubkey()
        )
        val toSub = postIds.minus(alreadyLiked.toSet())
        if (toSub.isEmpty()) return emptyMap()

        Log.i(TAG, "Return ${toSub.size} postIds to sub reactions")
        return relayProvider.getReadRelays().associateWith { _ -> toSub }
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

        return relayProvider.getReadRelays().associateWith { _ -> filtered.toMutableList() }
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

    private fun MutableList<String>.unsubThenAddAll(newSubIds: Collection<String>) {
        val snapshot = this.toList()
        this.clear()
        this.addAll(newSubIds)
        nostrSubscriber.unsubscribe(snapshot)
    }


    // **********************************************************
    private suspend fun submitFullProfiles(pubkeys: Collection<Pubkey>) {
        if (pubkeys.isEmpty()) return
        val myReadRelays = getMaxRelays(from = relayProvider.getReadRelays())
        val relays = relayProvider.getWriteRelaysByPubkeys(pubkeys = pubkeys)
        relays.forEach { (pubkey, relays) ->
            subQueue.submitFullProfile(
                pubkey = pubkey,
                relays = relays.ifEmpty { myReadRelays })
        }
    }

    private suspend fun submitSimpleUnknowns(notes: Collection<PostWithMeta>) {
        if (notes.isEmpty()) return

        val unknownAuthors = notes
            .filter { it.hasUnknownAuthor }
            .map { it.pubkey }
        val unknownReplyParents = notes
            .filter { it.replyToName == null }
            .mapNotNull { it.replyToPubkey }
        val unknownAuthorsOfMentionedPost = notes
            .flatMap { it.annotatedMentionedPosts }
            .filter { it.mentionedPost.name == null }
            .mapNotNull { it.mentionedPost.pubkey }

        val allPubkeys = listOf(unknownAuthors, unknownReplyParents, unknownAuthorsOfMentionedPost)
            .flatten()
            .toSet()
        submitFullProfiles(pubkeys = allPubkeys)

        val unknownMentionedNoteIds = notes
            .flatMap { it.annotatedMentionedPosts }
            .filter { it.mentionedPost.content == null }
            .map { it.mentionedPost.id }
        unknownMentionedNoteIds.forEach { subQueue.submitNoteId(noteId = it, relays = null) }
    }

    private suspend fun submitComplexUnknowns(notes: Collection<PostWithMeta>) {
        if (notes.isEmpty()) return

        val myReadRelays = relayProvider.getReadRelays()

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