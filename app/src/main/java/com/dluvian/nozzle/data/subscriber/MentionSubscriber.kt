package com.dluvian.nozzle.data.subscriber

import android.util.Log
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.nostr.utils.IdExtractorUtils
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.helper.BasePost
import com.dluvian.nozzle.model.helper.PubkeysAndAuthorPubkeys
import com.dluvian.nozzle.model.nostr.Nevent

private const val TAG = "MentionSubscriber"

class MentionSubscriber(
    private val nostrSubscriber: INostrSubscriber,
    private val relayProvider: IRelayProvider,
    private val idCache: IIdCache,
    private val postDao: PostDao,
) : IMentionSubscriber {
    override suspend fun subscribeMentionedPosts(basePosts: Collection<BasePost>): List<Nevent> {
        if (basePosts.isEmpty()) return emptyList()

        val mentionedPosts = IdExtractorUtils
            .extractNeventsAndNoteIds(contents = basePosts.map { it.content })
        val existingIds = postDao.filterExistingIds(postIds = mentionedPosts.map { it.eventId })
        val newMentionedPosts = mentionedPosts.filter { !existingIds.contains(it.eventId) }

        Log.i(TAG, "Subscribe ${newMentionedPosts.size} new mentioned posts")
        if (newMentionedPosts.isEmpty()) return emptyList()

        val postIdsByRelays = buildRelayMap(
            objs = newMentionedPosts,
            getId = { nevent -> nevent.eventId },
            getRelays = { nevent -> nevent.relays }
        )
        postIdsByRelays.forEach { entry ->
            nostrSubscriber.subscribePosts(
                postIds = entry.value.distinct(),
                relays = listOf(entry.key)
            )
        }

        return mentionedPosts
    }

    override suspend fun subscribeMentionedProfiles(
        basePosts: Collection<BasePost>
    ): PubkeysAndAuthorPubkeys {
        if (basePosts.isEmpty()) return PubkeysAndAuthorPubkeys()

        val foundAuthorPubkeys = basePosts.map { it.pubkey }
        val contents = basePosts.map { it.content }
        val mentionedNprofiles = IdExtractorUtils.extractNprofilesAndNpubs(contents = contents)
        val mentionedPubkeys = mentionedNprofiles.map { it.pubkey }.toSet()
        val existingPubkeys = idCache.getPubkeys().filter { mentionedPubkeys.contains(it) }
        val exludePubkeys = existingPubkeys
            .shuffled()
            .take(maxOf(1, existingPubkeys.size / 2))
        val profilesToSub = mentionedNprofiles.filter { !exludePubkeys.contains(it.pubkey) }

        Log.i(TAG, "Subscribe ${profilesToSub.size} mentioned profiles")
        if (profilesToSub.isEmpty()) {
            return PubkeysAndAuthorPubkeys(authorPubkeys = foundAuthorPubkeys)
        }

        val pubkeysByRelays = buildRelayMap(
            objs = profilesToSub,
            getId = { nprofile -> nprofile.pubkey },
            getRelays = { nprofile -> nprofile.relays }
        )
        pubkeysByRelays.forEach { entry ->
            nostrSubscriber.subscribeProfiles(
                pubkeys = entry.value.distinct(),
                relays = listOf(entry.key)
            )
        }

        return PubkeysAndAuthorPubkeys(
            pubkeys = mentionedPubkeys,
            authorPubkeys = foundAuthorPubkeys
        )
    }

    private fun <T> buildRelayMap(
        objs: List<T>,
        getId: (T) -> String,
        getRelays: (T) -> List<String>
    ): Map<String, List<String>> {
        val idsByRelays = mutableMapOf<String, MutableList<String>>()
        objs.forEach { obj ->
            getRelays(obj).forEach { relay ->
                val id = getId(obj)
                val ids = idsByRelays.putIfAbsent(relay, mutableListOf(id))
                ids?.add(id)
            }
        }
        val allIds = objs.map { getId(it) }.toMutableList()
        relayProvider.getReadRelays().forEach { relay ->
            val ids = idsByRelays.putIfAbsent(relay, allIds)
            ids?.addAll(allIds)
        }

        return idsByRelays
    }
}
