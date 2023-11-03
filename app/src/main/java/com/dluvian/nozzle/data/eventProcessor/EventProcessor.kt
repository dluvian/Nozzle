package com.dluvian.nozzle.data.eventProcessor

import android.util.Log
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.MAX_PROCESSING_DELAY
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.utils.KeyUtils
import com.dluvian.nozzle.data.room.AppDatabase
import com.dluvian.nozzle.data.room.entity.ContactEntity
import com.dluvian.nozzle.data.room.entity.HashtagEntity
import com.dluvian.nozzle.data.room.entity.MentionEntity
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.entity.ProfileEntity
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
    private val eventIdCache = Collections.synchronizedSet(mutableSetOf<String>())
    private var upperTimeBoundary = getUpperTimeBoundary()

    // Not a synchronized set bc we synchronize with `synchronized()`
    private val queue = mutableSetOf<RelayedEvent>()
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
        if (!isNewAndValid(event)) return

        val currentTime = System.currentTimeMillis()
        val items = mutableSetOf<RelayedEvent>()

        synchronized(queue) {
            queue.add(RelayedEvent(event = event, relayUrl = relayUrl))

            val delayElapsed = currentTime - lastProcessingTime.get() >= MAX_PROCESSING_DELAY
            if (queue.size < DB_BATCH_SIZE && !delayElapsed) return

            // TODO: bg task that takes every 1s

            items.addAll(queue.toSet())
            queue.clear()
        }
        if (items.isNotEmpty()) {
            lastProcessingTime.set(currentTime)
            processQueue(items = items)
        }
    }

    private fun isNewAndValid(event: Event): Boolean {
        return event.isPost() && verify(event) ||
                (event.isProfileMetadata() ||
                        event.isContactList() ||
                        event.isNip65() ||
                        event.isLikeReaction()
                        ) && !eventIdCache.contains(event.id) && verify(event)
    }

    private fun processQueue(items: Set<RelayedEvent>) {
        Log.i(TAG, "Process ${items.size} events")

        val posts = mutableListOf<RelayedEvent>()
        val profiles = mutableListOf<Event>()
        val contactLists = mutableListOf<Event>()
        val nip65s = mutableListOf<Event>()
        val reactions = mutableListOf<Event>()

        items.forEach {
            if (it.event.isPost()) posts.add(it)
            else if (it.event.isProfileMetadata()) profiles.add(it.event)
            else if (it.event.isContactList()) contactLists.add(it.event)
            else if (it.event.isNip65()) nip65s.add(it.event)
            else if (it.event.isLikeReaction()) reactions.add(it.event)
        }

        processPosts(relayedEvents = posts)
        processProfiles(events = profiles)
        processContactLists(events = contactLists)
        processNip65s(events = nip65s)

//        val reactions = items.filter { it.event.isLikeReaction() }
//        processReactions(reactions = reactions)
    }

    private fun processPosts(relayedEvents: Collection<RelayedEvent>) {
        if (relayedEvents.isEmpty()) return

        val alreadyPresent = relayedEvents
            .filter { dbSweepExcludingCache.containsPostId(it.event.id) }
        if (alreadyPresent.isNotEmpty()) insertEventRelays(relayedEvents = alreadyPresent)
        if (alreadyPresent.size == relayedEvents.size) return

        val newPosts = relayedEvents - alreadyPresent.toSet()
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

    private fun processProfiles(events: Collection<Event>) {
        if (events.isEmpty()) return

        val ids = events.map(Event::id)
        eventIdCache.addAll(ids)

        val metadata = events.associate { Pair(it.id, deserializeMetadata(it.content)) }

        val profileEntities = events
            .sortedByDescending { it.createdAt }
            .distinctBy(Event::pubkey)
            .mapNotNull { event ->
                metadata[event.id]?.let { meta ->
                    ProfileEntity(
                        pubkey = event.pubkey,
                        createdAt = event.createdAt,
                        metadata = meta
                    )
                }
            }

        if (profileEntities.isEmpty()) return

        scope.launch {
            database.profileDao().insertAndDeleteOutdated(profiles = profileEntities)
        }.invokeOnCompletion {
            if (it == null) {
                dbSweepExcludingCache.addPubkeys(events.map(Event::pubkey))
                return@invokeOnCompletion
            }
            Log.w(TAG, "Failed to process profiles", it)
            eventIdCache.removeAll(ids.toSet())
        }
    }

    private fun processContactLists(events: Collection<Event>) {
        if (events.isEmpty()) return

        val ids = events.map(Event::id)
        eventIdCache.addAll(ids)

        val contactEntities = events
            .sortedByDescending { it.createdAt }
            .distinctBy(Event::pubkey)
            .flatMap { event ->
                getContactPubkeys(event.tags).map { contactPubkey ->
                    ContactEntity(
                        pubkey = event.pubkey,
                        createdAt = event.createdAt,
                        contactPubkey = contactPubkey
                    )
                }
            }

        scope.launch {
            database.contactDao().insertAndDeleteOutdated(contacts = contactEntities)
        }.invokeOnCompletion {
            if (it == null) {
                val pubkeys = contactEntities.map(ContactEntity::pubkey)
                dbSweepExcludingCache.addContactListAuthors(pubkeys = pubkeys)
                return@invokeOnCompletion
            }
            Log.w(TAG, "Failed to process contact lists", it)
            eventIdCache.removeAll(ids.toSet())
        }
    }

    private fun processNip65s(events: Collection<Event>) {
        if (events.isEmpty()) return

        val ids = events.map(Event::id)
        eventIdCache.addAll(ids)

        val nip65Entities = events
            .sortedByDescending { it.createdAt }
            .distinctBy { it.pubkey }
            .flatMap { event ->
                event.getNip65Entries().map { entry ->
                    Nip65Entity(
                        pubkey = event.pubkey,
                        url = entry.url,
                        isRead = entry.isRead,
                        isWrite = entry.isWrite,
                        createdAt = event.createdAt,
                    )
                }
            }

        scope.launch {
            database.nip65Dao().insertAndDeleteOutdated(nip65s = nip65Entities)
        }.invokeOnCompletion {
            if (it == null) {
                val pubkeys = nip65Entities.map { it.pubkey }
                dbSweepExcludingCache.addNip65Authors(pubkeys = pubkeys)
                return@invokeOnCompletion
            }
            Log.w(TAG, "Failed to process nip65s", it)
            eventIdCache.removeAll(ids)
        }
    }

    private fun processReaction(event: Event) {
        if (event.content != "+" && event.content.isNotEmpty()) return
        if (eventIdCache.contains(event.id)) return
        if (!verify(event)) return

        eventIdCache.add(event.id)

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
            eventIdCache.remove(event.id)
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
        return runCatching { gson.fromJson(json, Metadata::class.java) }
            .onFailure { e -> Log.w(TAG, "Failed to deserialize $json", e) }
            .getOrNull()
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
