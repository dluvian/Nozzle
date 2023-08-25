package com.dluvian.nozzle.data.eventProcessor

import android.util.Log
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.utils.UrlUtils
import com.dluvian.nozzle.data.utils.hexToNpub
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
    private val reactionDao: ReactionDao,
    private val contactDao: ContactDao,
    private val profileDao: ProfileDao,
    private val postDao: PostDao,
    private val eventRelayDao: EventRelayDao,
    private val nip65Dao: Nip65Dao,
) : IEventProcessor {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val gson: Gson = GsonBuilder().disableHtmlEscaping().create()
    private val idCache = Collections.synchronizedSet(mutableSetOf<String>())

    override fun process(event: Event, relayUrl: String?) {
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
        insertEventRelay(eventId = event.id, relayUrl = relayUrl?.let { UrlUtils.cleanUrl(it) })

        val wasNew = idCache.add(event.id)
        if (!wasNew) return

        scope.launch {
            postDao.insertIfNotPresent(PostEntity.fromEvent(event))
        }
    }

    private fun processContactList(event: Event) {
        if (idCache.contains(event.id)) return
        if (!verify(event)) return

        idCache.add(event.id)

        scope.launch {
            contactDao.insertAndDeleteOutdated(
                pubkey = event.pubkey,
                newTimestamp = event.createdAt,
                contactPubkeys = getContactPubkeys(event.tags),
            )
        }
    }

    private fun processMetadata(event: Event) {
        if (idCache.contains(event.id)) return
        if (!verify(event)) return

        idCache.add(event.id)

        Log.d(TAG, "Process profile event ${event.content}")
        deserializeMetadata(event.content)?.let {
            scope.launch {
                profileDao.insertAndDeleteOutdated(
                    pubkey = event.pubkey,
                    newTimestamp = event.createdAt,
                    ProfileEntity(
                        pubkey = event.pubkey,
                        metadata = Metadata(
                            name = it.name.orEmpty().trim(),
                            about = it.about.orEmpty().trim(),
                            picture = it.picture.orEmpty().trim(),
                            nip05 = it.nip05.orEmpty().trim(),
                            lud16 = it.lud16.orEmpty().trim(),
                        ),
                        createdAt = event.createdAt,
                    )
                )
            }
        }
    }

    private fun processNip65(event: Event) {
        if (idCache.contains(event.id)) return

        val nip65Entries = event.getNip65Entries()
        if (nip65Entries.isEmpty()) return
        if (!verify(event)) return

        idCache.add(event.id)

        Log.d(TAG, "Process ${nip65Entries.size} nip65 entries from ${hexToNpub(event.pubkey)}")
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
            nip65Dao.insertAndDeleteOutdated(
                pubkey = event.pubkey,
                timestamp = event.createdAt,
                nip65Entities = entities.toTypedArray()
            )
        }
    }

    private fun processReaction(event: Event) {
        if (event.content != "+") return
        if (idCache.contains(event.id)) return
        if (!verify(event)) return

        idCache.add(event.id)

        event.getReactedToId()?.let {
            scope.launch {
                reactionDao.like(eventId = it, pubkey = event.pubkey)
            }
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
            Log.i(TAG, "Failed to deserialize $json")
        }
        return null
    }

    private fun getContactPubkeys(tags: List<Tag>): List<String> {
        return tags
            .filter { tag -> tag.size >= 2 && tag[0] == "p" }
            .map { tag -> tag[1] }
            .distinct()
    }

    private fun insertEventRelay(eventId: String, relayUrl: String?) {
        if (relayUrl == null) return

        val wasNew = idCache.add(eventId + relayUrl)
        if (!wasNew) return

        scope.launch {
            eventRelayDao.insertOrIgnore(eventId = eventId, relayUrl = relayUrl)
        }
    }
}
