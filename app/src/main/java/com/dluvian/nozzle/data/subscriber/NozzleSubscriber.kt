package com.dluvian.nozzle.data.subscriber

import android.util.Log
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.nostr.utils.KeyUtils
import com.dluvian.nozzle.data.nostr.utils.MentionUtils.extractNeventsAndNoteIds
import com.dluvian.nozzle.data.nostr.utils.MentionUtils.extractNprofilesAndNpubs
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.AppDatabase
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.utils.takeRandom80percent
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.MultipleRelays
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.RelaySelection
import com.dluvian.nozzle.model.UserSpecific
import com.dluvian.nozzle.model.helper.FeedInfo
import com.dluvian.nozzle.model.helper.IdAndRelays
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile
import com.dluvian.nozzle.model.nostr.ReplyTo
import java.util.Collections

private const val TAG = "NozzleSubscriber"

class NozzleSubscriber(
    private val nostrSubscriber: INostrSubscriber,
    private val relayProvider: IRelayProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val idCache: IIdCache,
    private val database: AppDatabase,
) : INozzleSubscriber {
    private val personalProfileSubs = Collections.synchronizedList(mutableListOf<String>())
    private val fullProfileSubs = Collections.synchronizedList(mutableListOf<String>())
    private val feedPostSubs = Collections.synchronizedList(mutableListOf<String>())
    private val feedInfoSubs = Collections.synchronizedList(mutableListOf<String>())
    private val nip65Subs = Collections.synchronizedList(mutableListOf<String>())
    private val threadPostSubs = Collections.synchronizedList(mutableListOf<String>())
    private val parentPostSubs = Collections.synchronizedList(mutableListOf<String>())
    private val unknownPubkeySubs = Collections.synchronizedList(mutableListOf<String>())

    override fun subscribePersonalProfile() {
        Log.i(TAG, "Subscribe personal profile")
        val subIds = nostrSubscriber.subscribeFullProfile(
            pubkey = pubkeyProvider.getPubkey(),
            relays = relayProvider.getWriteRelays()
        )
        personalProfileSubs.unsubThenAddAll(subIds)
    }

    override suspend fun subscribeFullProfile(profileId: String) {
        Log.i(TAG, "Subscribe full profile of $profileId")
        val nostrId = EncodingUtils.profileIdToNostrId(profileId = profileId)
        val hex = nostrId?.hex ?: profileId
        val recommendedRelays = nostrId?.recommendedRelays.orEmpty()
        val relays = relayProvider.getReadRelays() +
                relayProvider.getWriteRelaysOfPubkey(pubkey = hex) +
                recommendedRelays

        val subIds = nostrSubscriber.subscribeFullProfile(pubkey = hex, relays = relays)
        fullProfileSubs.unsubThenAddAll(subIds)
    }

    override fun subscribeToFeedPosts(
        isReplies: Boolean,
        hashtag: String?,
        authorPubkeys: List<String>?,
        limit: Int,
        relaySelection: RelaySelection,
        until: Long
    ) {
        Log.i(TAG, "Subscribe feed posts")
        if (authorPubkeys != null && authorPubkeys.isEmpty()) return

        val subIds = when (relaySelection) {
            is AllRelays, is MultipleRelays -> {
                nostrSubscriber.subscribeToFeedPosts(
                    authorPubkeys = authorPubkeys,
                    hashtag = hashtag,
                    limit = limit,
                    until = until,
                    relays = relaySelection.selectedRelays
                )
            }

            is UserSpecific -> {
                if (authorPubkeys == null) {
                    nostrSubscriber.subscribeToFeedPosts(
                        authorPubkeys = null,
                        hashtag = hashtag,
                        limit = limit,
                        until = until,
                        relays = relaySelection.selectedRelays
                    )
                } else {
                    relaySelection.pubkeysPerRelay.flatMap { (relay, pubkeys) ->
                        // We ignore authorPubkeys because relaySelection should contain them
                        // TODO: Bad design. IMPROVE
                        if (pubkeys.isNotEmpty()) {
                            nostrSubscriber.subscribeToFeedPosts(
                                authorPubkeys = pubkeys.toList(),
                                hashtag = hashtag,
                                limit = limit,
                                until = until,
                                relays = listOf(relay)
                            )
                        } else emptyList()
                    }
                }
            }
        }
        feedPostSubs.unsubThenAddAll(subIds)
    }

    override fun subscribeToInbox(relays: Collection<String>, limit: Int, until: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun subscribeFeedInfo(posts: List<PostEntity>): FeedInfo {
        Log.i(TAG, "Subscribe feed info")
        if (posts.isEmpty()) return FeedInfo.createEmpty()

        val postIds = posts.map { it.id }
        val authorPubkeys = posts.map { it.pubkey }.distinct()
        val contents = posts.map { it.content }
        val mentionedProfiles = extractNprofilesAndNpubs(contents = contents)
        val mentionedPubkeys = mentionedProfiles.map { it.pubkey }
        val mentionedPosts = extractNeventsAndNoteIds(contents = contents)
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
            repliesByRelay = getPostIdsToSubReplies(postIds = postIds),
            reactionPostIdsByRelay = getReactionPostIdsToSub(postIds = postIds),
            reactorPubkey = pubkeyProvider.getPubkey()
        )
        feedInfoSubs.unsubThenAddAll(subIds)

        return FeedInfo(
            postIds = postIds,
            authorPubkeys = authorPubkeys,
            mentionedPubkeys = mentionedPubkeys,
            mentionedPostIds = mentionedPostIds,
        )
    }

    override suspend fun subscribeUnknowns(posts: List<PostWithMeta>) {
        Log.d(TAG, "Enter subscribeUnknowns")
        if (posts.isEmpty()) return

        val replyParentPubkeys = posts.mapNotNull { it.replyToPubkey }
        val mentionedPostPubkeys = posts
            .flatMap { it.annotatedMentionedPosts }
            .map { it.mentionedPost.pubkey }
        val allPubkeys = (replyParentPubkeys + mentionedPostPubkeys).distinct()
        if (allPubkeys.isEmpty()) return

        val existingPubkeys = database.profileDao().filterExistingPubkeys(pubkeys = allPubkeys)
        val unknownPubkeys = allPubkeys.minus(existingPubkeys.toSet())
        Log.i(TAG, "Subscribe ${unknownPubkeys.size} unknown pubkeys")
        if (unknownPubkeys.isEmpty()) return

        val nip65RelayMapping = relayProvider.getWriteRelaysOfPubkeys(unknownPubkeys)
            .map { IdAndRelays(id = it.key, relays = it.value) }

        val subIds = nostrSubscriber.subscribeNip65AndProfiles(
            nip65PubkeysByRelay = getNip65PubkeysToSub(
                pubkeys = unknownPubkeys,
                nprofiles = emptyList()
            ),
            pubkeysByRelay = relayProvider.getReadRelays()
                .associateWith { _ -> unknownPubkeys.toMutableList() }
                .toMutableMap()
                .addSpecialRelayMapping(nip65RelayMapping)
        )
        unknownPubkeySubs.unsubThenAddAll(subIds)
    }

    override suspend fun subscribeThreadPost(postId: String) {
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

    override suspend fun subscribeNip65(pubkeys: List<String>) {
        if (pubkeys.isEmpty()) return

        Log.i(TAG, "Subscribe nip65 of ${pubkeys.size} pubkeys")
        val filteredPubkeys = pubkeys.minus(idCache.getNip65Authors())
        if (filteredPubkeys.isEmpty()) return

        val pubkeysInDb = database.nip65Dao().filterPubkeysWithNip65(pubkeys = filteredPubkeys)
        val toSub = filteredPubkeys.minus(pubkeysInDb.takeRandom80percent().toSet())
        if (toSub.isEmpty()) return

        val subIds = nostrSubscriber.subscribeNip65(pubkeys = toSub)
        nip65Subs.unsubThenAddAll(subIds)
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
                database.contactDao().filterFriendsWithList(
                    contactPubkeys = filteredPubkeys,
                    myPubkey = pubkeyProvider.getPubkey()
                )
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

    private fun getPostIdsToSubReplies(postIds: List<String>): Map<Relay, List<String>> {
        return if (postIds.isEmpty()) emptyMap()
        else relayProvider.getReadRelays().associateWith { _ -> postIds }
    }

    private suspend fun getReactionPostIdsToSub(postIds: List<String>): Map<Relay, List<String>> {
        if (postIds.isEmpty()) return emptyMap()

        val alreadyLiked = database.reactionDao().filterLikedPostIds(
            postIds = postIds,
            pubkey = pubkeyProvider.getPubkey()
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
}
