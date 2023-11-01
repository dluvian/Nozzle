package com.dluvian.nozzle.data.eventProcessor

import android.util.Log
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.MAX_PROCESSING_DELAY
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.utils.KeyUtils
import com.dluvian.nozzle.data.room.AppDatabase
import com.dluvian.nozzle.data.room.entity.HashtagEntity
import com.dluvian.nozzle.data.room.entity.MentionEntity
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.utils.JsonUtils.gson
import com.dluvian.nozzle.data.utils.TimeConstants
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.model.nostr.RelayedEvent
import com.dluvian.nozzle.model.nostr.Tag
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.atomic.AtomicLong

private const val TAG = "EventProcessor"

class EventProcessor(
    private val dbSweepExcludingCache: IIdCache,
    private val database: AppDatabase,
) : IEventProcessor {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val otherIdsCache = Collections.synchronizedSet(mutableSetOf<String>())
    private var upperTimeBoundary = getUpperTimeBoundary()

    private val maxBatchSize = DB_BATCH_SIZE
    private val queue = Collections.synchronizedSet(mutableSetOf<RelayedEvent>())
    private val lastProcessingTime = AtomicLong(0L)


    override fun submit(event: Event, relayUrl: String?) {
        if (relayUrl == null) {
            Log.w(TAG, "Origin relay of ${event.id} is unknown")
            return
        }
        if (isFromFuture(event)) {
            Log.w(TAG, "Discard event from the future ${event.id}")
            return
        }

        if (isSubmittable(event)) {
            queue.add(RelayedEvent(event = event, relayUrl = relayUrl))
        }

        val currentTime = System.currentTimeMillis()
        val items = mutableSetOf<RelayedEvent>()
        synchronized(queue) {
            val delayElapsed = currentTime - lastProcessingTime.get() >= MAX_PROCESSING_DELAY
            if (queue.size < maxBatchSize && !delayElapsed) return

            // TODO: bg task that takes every 1s
            items.addAll(queue.toSet())
            queue.clear()
        }
        lastProcessingTime.set(currentTime)
        processQueue(items = items)
    }

    private fun isSubmittable(event: Event): Boolean {
        return event.isPost() && verify(event) ||
                event.isProfileMetadata() && !otherIdsCache.contains(event.id) && verify(event) ||
                event.isContactList() && !otherIdsCache.contains(event.id) && verify(event) ||
                event.isNip65() && !otherIdsCache.contains(event.id) && verify(event) ||
                event.isLikeReaction() && !otherIdsCache.contains(event.id) && verify(event)
    }

    private fun processQueue(items: Set<RelayedEvent>) {
        Log.i(TAG, "Process ${items.size} events")

        val posts = items.filter { it.event.isPost() }
        processPosts(posts = posts)

        val profiles = items.filter { it.event.isProfileMetadata() }
//        processProfiles(profiles = profiles)

//        val contactLists = items.filter { it.event.isContactList() }
//        processContactLists(contactLists = contactLists)
//
//        val nip65s = items.filter { it.event.isNip65() }
//        processContactLists(nip65s = nip65s)
//
//        val reactions = items.filter { it.event.isLikeReaction() }
//        processReactions(reactions = reactions)

    }

    private fun processPosts(posts: Collection<RelayedEvent>) {
        if (posts.isEmpty()) return

        val alreadyPresent = posts.filter { dbSweepExcludingCache.containsPostId(it.event.id) }
        if (alreadyPresent.isNotEmpty()) insertEventRelays(relayedEvents = alreadyPresent)
        if (alreadyPresent.size == posts.size) return

        val newPosts = posts - alreadyPresent.toSet()
        val postEntities = newPosts.map { PostEntity.fromEvent(it.event) }
        val hashtags = newPosts.flatMap { HashtagEntity.fromEvent(it.event) }
        val mentions = newPosts.flatMap { MentionEntity.fromEvent(it.event) }

        scope.launch {
            database.postDao().insertWithHashtagsAndMentions(
                posts = postEntities,
                hashtags = hashtags,
                mentions = mentions,
                hashtagDao = database.hashtagDao(),
                mentionDao = database.mentionDao(),
            )
        }.invokeOnCompletion { exception ->
            if (exception == null) {
                dbSweepExcludingCache.addPostIds(newPosts.map { post -> post.event.id })
                insertEventRelays(relayedEvents = newPosts)
                return@invokeOnCompletion
            }
            Log.w(TAG, "Failed to process posts", exception)
        }
    }

    private fun processProfiles(profiles: Collection<Event>) {
        if (profiles.isEmpty()) return

        val metadata = profiles.associate { Pair(it.id, deserializeMetadata(it.content)) }

//        scope.launch {
//            database.profileDao().insertAndDeleteOutdated(
//                pubkey = event.pubkey,
//                newTimestamp = event.createdAt,
//                ProfileEntity(
//                    pubkey = event.pubkey,
//                    metadata = Metadata(
//                        name = metadata.name.orEmpty().trim(),
//                        about = metadata.about.orEmpty().trim(),
//                        picture = metadata.picture.orEmpty().trim(),
//                        nip05 = metadata.nip05.orEmpty().trim(),
//                        lud16 = metadata.lud16.orEmpty().trim(),
//                    ),
//                    createdAt = event.createdAt,
//                )
//            )
//        }.invokeOnCompletion {
//            if (it == null) {
//                dbSweepExcludingCache.addPubkey(event.pubkey)
//                return@invokeOnCompletion
//            }
//            Log.w(TAG, "Failed to process profile ${event.id} of ${event.pubkey}", it)
//            otherIdsCache.remove(event.id)
//        }
    }

    private fun processContactList(event: Event) {
        if (otherIdsCache.contains(event.id)) return
        if (!verify(event)) return

        otherIdsCache.add(event.id)

        scope.launch {
            database.contactDao().insertAndDeleteOutdated(
                pubkey = event.pubkey,
                newTimestamp = event.createdAt,
                contactPubkeys = getContactPubkeys(event.tags),
            )
        }.invokeOnCompletion {
            if (it == null) {
                dbSweepExcludingCache.addContactListAuthor(event.pubkey)
                return@invokeOnCompletion
            }
            Log.w(TAG, "Failed to process contact list ${event.id} from ${event.pubkey}", it)
            otherIdsCache.remove(event.id)
        }
    }

    private fun processNip65(event: Event) {
        if (otherIdsCache.contains(event.id)) return

        val nip65Entries = event.getNip65Entries()
        if (nip65Entries.isEmpty()) return
        if (!verify(event)) return

        otherIdsCache.add(event.id)

        scope.launch {
            val entities = nip65Entries.map {
                Nip65Entity(
                    pubkey = event.pubkey,
                    url = it.url,
                    isRead = it.isRead,
                    isWrite = it.isWrite,
                    createdAt = event.createdAt,
                )
            }
            database.nip65Dao().insertAndDeleteOutdated(
                pubkey = event.pubkey,
                timestamp = event.createdAt,
                nip65Entities = entities.toTypedArray()
            )
        }.invokeOnCompletion {
            if (it == null) {
                dbSweepExcludingCache.addNip65Author(event.pubkey)
                return@invokeOnCompletion
            }
            Log.w(TAG, "Failed to process nip65 ${event.id} of ${event.pubkey}", it)
            otherIdsCache.remove(event.id)
        }
    }

    private fun processReaction(event: Event) {
        if (event.content != "+" && event.content.isNotEmpty()) return
        if (otherIdsCache.contains(event.id)) return
        if (!verify(event)) return

        otherIdsCache.add(event.id)

        val reactedToId = event.getReactedToId() ?: return

        scope.launch {
            runCatching {
                database.reactionDao().like(eventId = reactedToId, pubkey = event.pubkey)
            }.onFailure {
                Log.w(TAG, "Failed to insert reaction ${event.id} from ${event.pubkey}")
            }
        }.invokeOnCompletion {
            if (it == null) return@invokeOnCompletion
            Log.w(TAG, "Failed to process reaction ${event.id} from ${event.pubkey}", it)
            otherIdsCache.remove(event.id)
        }
    }

    private fun verify(event: Event): Boolean {
        val isValid = event.verify()
        if (!isValid) {
            Log.w(TAG, "Invalid event kind ${event.kind} id ${event.id} ")
        }
        return isValid
    }

    private fun deserializeMetadata(json: String): Metadata? {
        try {
            return gson.fromJson(json, Metadata::class.java)
        } catch (t: Throwable) {
            Log.w(TAG, "Failed to deserialize $json")
        }
        return null
    }

    private fun getContactPubkeys(tags: List<Tag>): List<String> {
        return tags
            .filter { tag -> tag.size >= 2 && tag[0] == "p" }
            .map { tag -> tag[1] }
            .filter { KeyUtils.isValidHexKey(hexKey = it) }
            .toList()
    }

    private fun insertEventRelays(relayedEvents: Collection<RelayedEvent>) {
//        val cleanUrlMap = events.
//        val specialIds = events.map { it.event.id + }
//
//        val cleanRelayUrl = relayUrl?.removeTrailingSlashes() ?: return
//
//
//        val specialId = eventId + cleanRelayUrl
//        val isNew = otherIdsCache.add(specialId)
//        if (!isNew) return
//
//        scope.launch {
//            database.eventRelayDao().insertOrIgnore(eventId = eventId, relayUrl = cleanRelayUrl)
//        }.invokeOnCompletion {
//            if (it == null) return@invokeOnCompletion
//            Log.w(TAG, "Failed to process eventRelay $cleanRelayUrl of event $eventId", it)
//            otherIdsCache.remove(specialId)
//        }
    }

    private fun getUpperTimeBoundary(): Long {
        return getCurrentTimeInSeconds() + TimeConstants.MINUTE_IN_SECONDS
    }

    private fun isFromFuture(event: Event): Boolean {
        if (event.createdAt > upperTimeBoundary) {
            upperTimeBoundary = getUpperTimeBoundary()
            return event.createdAt > upperTimeBoundary
        }
        return false
    }
}
