package com.dluvian.nozzle.data.eventProcessor

import android.util.Log
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.utils.KeyUtils
import com.dluvian.nozzle.data.room.AppDatabase
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.utils.TimeConstants
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.model.nostr.Tag
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Collections

private const val TAG = "EventProcessor"

class EventProcessor(
    private val dbSweepExcludingCache: IIdCache,
    private val database: AppDatabase,
) : IEventProcessor {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson: Gson = GsonBuilder().disableHtmlEscaping().create()
    private val otherIdsCache = Collections.synchronizedSet(mutableSetOf<String>())
    private var upperTimeBoundary = getUpperTimeBoundary()

    override fun process(event: Event, relayUrl: String?) {
        if (isFromFuture(event)) {
            Log.w(TAG, "Discard event from the future ${event.id}")
            return
        }
        if (event.isPost()) {
            processPost(event = event, relayUrl = relayUrl)
            return
        }
        if (event.isProfileMetadata()) {
            processMetadata(event = event)
            return
        }
        if (event.isContactList()) {
            processContactList(event = event)
            return
        }
        if (event.isNip65()) {
            processNip65(event = event)
            return
        }
        if (event.isReaction()) {
            processReaction(event = event)
            return
        }
    }

    private fun processPost(event: Event, relayUrl: String?) {
        if (!verify(event)) return

        val isPresent = dbSweepExcludingCache.containsPostId(event.id)
        if (isPresent) {
            insertEventRelay(eventId = event.id, relayUrl = relayUrl)
            return
        }
        scope.launch {
            database.postDao().insertWithHashtagsAndMentions(
                postEntity = PostEntity.fromEvent(event),
                hashtagDao = database.hashtagDao(),
                hashtags = event.getHashtags(),
                mentionDao = database.mentionDao(),
                mentions = event.getMentions()
            )
        }.invokeOnCompletion {
            if (it == null) {
                dbSweepExcludingCache.addPostId(event.id)
                insertEventRelay(eventId = event.id, relayUrl = relayUrl)
                return@invokeOnCompletion
            }
            Log.w(TAG, "Failed to process post ${event.id} from ${event.pubkey}", it)
        }
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

    private fun processMetadata(event: Event) {
        if (otherIdsCache.contains(event.id)) return
        if (!verify(event)) return

        otherIdsCache.add(event.id)

        val metadata = deserializeMetadata(event.content) ?: return

        scope.launch {
            database.profileDao().insertAndDeleteOutdated(
                pubkey = event.pubkey,
                newTimestamp = event.createdAt,
                ProfileEntity(
                    pubkey = event.pubkey,
                    metadata = Metadata(
                        name = metadata.name.orEmpty().trim(),
                        about = metadata.about.orEmpty().trim(),
                        picture = metadata.picture.orEmpty().trim(),
                        nip05 = metadata.nip05.orEmpty().trim(),
                        lud16 = metadata.lud16.orEmpty().trim(),
                    ),
                    createdAt = event.createdAt,
                )
            )
        }.invokeOnCompletion {
            if (it == null) {
                dbSweepExcludingCache.addPubkey(event.pubkey)
                return@invokeOnCompletion
            }
            Log.w(TAG, "Failed to process profile ${event.id} of ${event.pubkey}", it)
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

    private fun insertEventRelay(eventId: String, relayUrl: String?) {
        val cleanRelayUrl = relayUrl?.removeTrailingSlashes() ?: return

        val specialId = eventId + cleanRelayUrl
        val isNew = otherIdsCache.add(specialId)
        if (!isNew) return

        scope.launch {
            database.eventRelayDao().insertOrIgnore(eventId = eventId, relayUrl = cleanRelayUrl)
        }.invokeOnCompletion {
            if (it == null) return@invokeOnCompletion
            Log.w(TAG, "Failed to process eventRelay $cleanRelayUrl of event $eventId", it)
            otherIdsCache.remove(specialId)
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
