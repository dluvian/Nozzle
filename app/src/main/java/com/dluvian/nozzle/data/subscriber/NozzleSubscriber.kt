package com.dluvian.nozzle.data.subscriber

import android.util.Log
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.nostr.utils.IdExtractorUtils
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.helper.BasePost
import com.dluvian.nozzle.model.helper.PubkeysAndAuthorPubkeys
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile

private const val TAG = "NozzleSubscriber"

class NozzleSubscriber(
    private val nostrSubscriber: INostrSubscriber,
    private val relayProvider: IRelayProvider,
    private val idCache: IIdCache,
    private val postDao: PostDao,
    private val profileDao: ProfileDao,
) : INozzleSubscriber {
    override suspend fun subscribeMentionedPosts(basePosts: Collection<BasePost>): List<Nevent> {
        if (basePosts.isEmpty()) return emptyList()

        val mentionedPosts = IdExtractorUtils
            .extractNeventsAndNoteIds(contents = basePosts.map { it.content })

        subscribeNewPosts(mentionedPosts = mentionedPosts)

        return mentionedPosts
    }

    override suspend fun subscribeMentionedProfiles(
        basePosts: Collection<BasePost>
    ): PubkeysAndAuthorPubkeys {
        if (basePosts.isEmpty()) return PubkeysAndAuthorPubkeys()

        val contents = basePosts.map { it.content }
        val mentionedNprofiles = IdExtractorUtils.extractNprofilesAndNpubs(contents = contents)
        val mentionedPubkeys = subscribeNewProfilesReturnMentionedPubkeys(
            mentionedProfiles = mentionedNprofiles
        )

        return PubkeysAndAuthorPubkeys(
            pubkeys = mentionedPubkeys,
            authorPubkeys = basePosts.map { it.pubkey }
        )
    }

    override suspend fun subscribeNewProfiles(pubkeys: Set<String>) {
        if (pubkeys.isEmpty()) return

        val pubkeysToSub = pubkeys.minus(getExistingPubkeys(pubkeys = pubkeys))
        Log.i(TAG, "Subscribe ${pubkeysToSub.size} new profiles")
        if (pubkeysToSub.isEmpty()) return

        val nip65 = relayProvider.getWriteRelaysOfPubkeys(pubkeys = pubkeysToSub)
        Log.d(TAG, "Found nip65 for ${nip65.size}/${pubkeysToSub.size} pubkeys")

        val pubkeysByRelays = buildRelayMap(
            objs = pubkeysToSub,
            getId = { pubkey -> pubkey },
            getRelays = { pubkey -> nip65[pubkey].orEmpty() }
        )
        pubkeysByRelays.forEach { entry ->
            nostrSubscriber.subscribeProfiles(
                pubkeys = entry.value.distinct(),
                relays = listOf(entry.key)
            )
        }

    }

    private suspend fun subscribeNewPosts(mentionedPosts: List<Nevent>) {
        if (mentionedPosts.isEmpty()) return

        val existingIds = postDao.filterExistingIds(postIds = mentionedPosts.map { it.eventId })
        val newMentionedPosts = mentionedPosts.filter { !existingIds.contains(it.eventId) }

        Log.i(TAG, "Subscribe ${newMentionedPosts.size} new mentioned posts")
        if (newMentionedPosts.isEmpty()) return

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
    }

    private suspend fun subscribeNewProfilesReturnMentionedPubkeys(
        mentionedProfiles: List<Nprofile>
    ): Set<String> {
        if (mentionedProfiles.isEmpty()) return emptySet()

        val mentionedPubkeys = mentionedProfiles.map { it.pubkey }.toSet()
        val excludePubkeys = getExistingPubkeys(pubkeys = mentionedPubkeys)
        val profilesToSub = mentionedProfiles.filter { !excludePubkeys.contains(it.pubkey) }

        Log.i(TAG, "Subscribe ${profilesToSub.size} new mentioned profiles")

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

        return mentionedPubkeys
    }

    private suspend fun getExistingPubkeys(pubkeys: Collection<String>): Set<String> {
        return idCache.getPubkeys() + filterPubkeysFromDb(pubkeys = pubkeys)
    }

    private suspend fun filterPubkeysFromDb(pubkeys: Collection<String>): List<String> {
        if (pubkeys.isEmpty()) return emptyList()

        val existingPubkeys = profileDao.filterExistingPubkeys(pubkeys)
        val take80Percent = maxOf(1.0, existingPubkeys.size * 0.8).toInt()
        Log.i(
            TAG,
            "Take $take80Percent pubkeys from ${existingPubkeys.size}/${pubkeys.size} in database"
        )
        return existingPubkeys.shuffled().take(take80Percent)
    }

    private fun <T> buildRelayMap(
        objs: Collection<T>,
        getId: (T) -> String,
        getRelays: (T) -> List<String>
    ): Map<String, List<String>> {
        if (objs.isEmpty()) return emptyMap()
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
