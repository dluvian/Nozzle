package com.dluvian.nozzle.data.eventProcessor

import android.util.Log
import com.dluvian.nozzle.data.EVENT_PROCESSING_DELAY
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.room.AppDatabase
import com.dluvian.nozzle.data.room.entity.ContactEntity
import com.dluvian.nozzle.data.room.entity.EventRelayEntity
import com.dluvian.nozzle.data.room.entity.HashtagEntity
import com.dluvian.nozzle.data.room.entity.MentionEntity
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.room.entity.ReactionEntity
import com.dluvian.nozzle.data.utils.JsonUtils.gson
import com.dluvian.nozzle.data.utils.TimeConstants
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.model.nostr.RelayedEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.atomic.AtomicBoolean

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
    private val isProcessingEvents = AtomicBoolean(false)

    init {
        startProcessingJob()
    }

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

        synchronized(queue) {
            queue.add(RelayedEvent(event = event, relayUrl = relayUrl))
        }
        if (!isProcessingEvents.get()) startProcessingJob()
    }

    private fun startProcessingJob() {
        if (!isProcessingEvents.compareAndSet(false, true)) return
        Log.i(TAG, "Start job")
        scope.launch {
            while (true) {
                delay(EVENT_PROCESSING_DELAY)

                val items = mutableSetOf<RelayedEvent>()
                synchronized(queue) {
                    items.addAll(queue.toSet())
                    queue.clear()
                }
                processQueue(items = items)
            }
        }.invokeOnCompletion {
            Log.w(TAG, "Processing job completed", it)
            isProcessingEvents.set(false)
        }
    }

    private fun processQueue(items: Set<RelayedEvent>) {
        if (items.isEmpty()) return
        Log.d(TAG, "Process queue of ${items.size} events")

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
        processReactions(events = reactions)
    }

    private fun isNewAndValid(event: Event): Boolean {
        return event.isPost() && verify(event) ||
                (event.isProfileMetadata() ||
                        event.isContactList() ||
                        event.isNip65() ||
                        event.isLikeReaction()
                        ) && !eventIdCache.contains(event.id) && verify(event)
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
            if (exception != null) {
                Log.w(TAG, "Failed to process posts", exception)
                return@invokeOnCompletion
            }
            dbSweepExcludingCache.addPostIds(newPosts.map { post -> post.event.id })
            insertEventRelays(relayedEvents = newPosts)
        }
    }

    private fun processProfiles(events: Collection<Event>) {
        if (events.isEmpty()) return

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
            if (it != null) {
                Log.w(TAG, "Failed to process profiles", it)
                return@invokeOnCompletion
            }
            eventIdCache.addAll(elements = events.map(Event::id))
            dbSweepExcludingCache.addPubkeys(events.map(Event::pubkey))
        }
    }

    private fun processContactLists(events: Collection<Event>) {
        if (events.isEmpty()) return

        val contactEntities = events
            .sortedByDescending { it.createdAt }
            .distinctBy(Event::pubkey)
            .flatMap { event ->
                event.getContactPubkeys().map { contactPubkey ->
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
            if (it != null) {
                Log.w(TAG, "Failed to process contact lists", it)
                return@invokeOnCompletion
            }
            eventIdCache.addAll(elements = events.map(Event::id))
            val pubkeys = contactEntities.map(ContactEntity::pubkey)
            dbSweepExcludingCache.addContactListAuthors(pubkeys = pubkeys)
        }
    }

    private fun processNip65s(events: Collection<Event>) {
        if (events.isEmpty()) return

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
            if (it != null) {
                Log.w(TAG, "Failed to process nip65s", it)
                return@invokeOnCompletion
            }
            eventIdCache.addAll(elements = events.map(Event::id))
            dbSweepExcludingCache.addNip65Authors(pubkeys = nip65Entities.map(Nip65Entity::pubkey))
        }
    }

    private fun processReactions(events: Collection<Event>) {
        if (events.isEmpty()) return

        val reactionEntities = events.mapNotNull { event ->
            val reactedToId = event.getReactedToId()
            if (reactedToId == null) null
            else ReactionEntity(eventId = reactedToId, pubkey = event.pubkey)
        }
        if (reactionEntities.isEmpty()) return

        scope.launch {
            runCatching {
                database.reactionDao()
                    .insertOrIgnore(reactionEntities = reactionEntities.toTypedArray())
            }.onFailure {
                Log.w(TAG, "Failed to insert reaction")
            }
        }.invokeOnCompletion {
            if (it != null) {
                Log.w(TAG, "Failed to process reactions", it)
                return@invokeOnCompletion
            }
            eventIdCache.addAll(elements = events.map(Event::id))
        }
    }

    private fun verify(event: Event): Boolean {
        val isValid = event.verify()
        if (!isValid) Log.w(TAG, "Invalid event kind=${event.kind} id=${event.id} ")

        return isValid
    }

    private fun deserializeMetadata(json: String): Metadata? {
        return runCatching { gson.fromJson(json, Metadata::class.java) }
            .onFailure { e -> Log.w(TAG, "Failed to deserialize $json", e) }
            .getOrNull()
    }

    private fun insertEventRelays(relayedEvents: Collection<RelayedEvent>) {
        if (relayedEvents.isEmpty()) return

        val eventRelayEntities = relayedEvents.map {
            EventRelayEntity(eventId = it.event.id, relayUrl = it.relayUrl.removeTrailingSlashes())
        }

        scope.launch {
            database.eventRelayDao().insertOrIgnore(*eventRelayEntities.toTypedArray())
        }.invokeOnCompletion { exception ->
            if (exception != null) {
                Log.w(TAG, "Failed to process eventRelays", exception)
                return@invokeOnCompletion
            }
            val specialIds = eventRelayEntities.map { "${it.eventId}${it.relayUrl}" }
            eventIdCache.addAll(specialIds)
        }
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
