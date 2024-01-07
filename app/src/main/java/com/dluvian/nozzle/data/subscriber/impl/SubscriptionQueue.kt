package com.dluvian.nozzle.data.subscriber.impl

import android.util.Log
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.subscriber.ISubscriptionQueue
import com.dluvian.nozzle.data.utils.addOrCreate
import com.dluvian.nozzle.data.utils.getMaxRelays
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.nostr.Filter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

private const val TAG = "SubscriptionQueue"

class SubscriptionQueue(
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider
) : ISubscriptionQueue {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val jobTerminated = AtomicBoolean(false)
    private val lastProcessTime = AtomicLong(0L)

    // Not a synchronized map bc we synchronize with `synchronized()`
    // Empty Relay when null
    private val queue = mutableMapOf<Relay, MutableSet<Filter>>()

    init {
        startProcessingJob()
    }


    override fun submitNoteIds(noteIds: List<NoteId>, relays: Collection<Relay>?) {
        if (noteIds.isEmpty() || relays?.isEmpty() == true) return

        val noteFilter = Filter.createNoteFilter(ids = noteIds)
        queue.syncedAddOrCreate(relays = relays, filter = noteFilter)
    }

    override fun submitReplies(parentIds: List<NoteId>, relays: Collection<Relay>?) {
        if (parentIds.isEmpty() || relays?.isEmpty() == true) return

        val replyFilter = Filter.createNoteFilter(e = parentIds)
        queue.syncedAddOrCreate(relays = relays, filter = replyFilter)

    }

    override fun submitProfiles(pubkeys: List<Pubkey>, relays: Collection<Relay>?) {
        if (pubkeys.isEmpty() || relays?.isEmpty() == true) return

        val profileFilter = Filter.createProfileFilter(pubkeys = pubkeys)
        queue.syncedAddOrCreate(relays = relays, filter = profileFilter)
    }

    override fun submitNip65s(pubkeys: List<Pubkey>, relays: Collection<Relay>?) {
        if (pubkeys.isEmpty() || relays?.isEmpty() == true) return

        val nip65Filter = Filter.createNip65Filter(pubkeys = pubkeys)
        queue.syncedAddOrCreate(relays = relays, filter = nip65Filter)
    }

    override fun submitContactLists(pubkeys: List<Pubkey>, relays: Collection<Relay>?) {
        if (pubkeys.isEmpty() || relays?.isEmpty() == true) return

        val contactFilter = Filter.createContactListFilter(pubkeys = pubkeys)
        queue.syncedAddOrCreate(relays = relays, filter = contactFilter)
    }

    override fun submitNotes(
        until: Long,
        limit: Int,
        authors: List<Pubkey>?,
        hashtag: String?,
        mentionedPubkey: Pubkey?,
        relays: Collection<Relay>?
    ) {
        if (limit <= 0 || authors?.isEmpty() == true || relays?.isEmpty() == true) return

        val noteFilter = Filter.createNoteFilter(
            pubkeys = authors,
            p = if (mentionedPubkey == null) null else listOf(mentionedPubkey),
            t = if (hashtag == null) null else listOf(hashtag),
            until = until,
            limit = limit,
        )
        queue.syncedAddOrCreate(relays = relays, filter = noteFilter)
    }

    override fun submitLikes(limit: Int, until: Long, author: Pubkey, relays: Collection<Relay>?) {
        if (limit <= 0 || relays?.isEmpty() == true) return

        val reactionFilter = Filter.createReactionFilter(pubkeys = listOf(author))
        queue.syncedAddOrCreate(relays = relays, filter = reactionFilter)
    }

    override fun submitLikes(
        noteIds: List<NoteId>,
        author: Pubkey,
        relays: Collection<Relay>?
    ) {
        if (noteIds.isEmpty() || relays?.isEmpty() == true) return

        val reactionFilter = Filter.createReactionFilter(pubkeys = listOf(author), e = noteIds)
        queue.syncedAddOrCreate(relays = relays, filter = reactionFilter)
    }

    override fun processNow() {
        if (jobTerminated.compareAndSet(true, false)) startProcessingJob()
        val currentQueue: Map<Relay, MutableSet<Filter>>
        synchronized(queue) {
            currentQueue = queue.toMap()
            queue.clear()
        }
        if (currentQueue.isEmpty()) return

        Log.i(TAG, "Process queue of ${currentQueue.size} relays")

        currentQueue.forEach { (relay, filters) ->
            val relays = if (relay.isEmpty()) {
                val rndRelays = getMaxRelays(from = nostrService.getActiveRelays()).toSet()
                rndRelays + getMaxRelays(from = relayProvider.getReadRelays())
            } else listOf(relay)

            val batchedFilters = batchFilters(filters = filters)
            relays.forEach { adjustedRelay ->
                nostrService.subscribe(
                    filters = batchedFilters,
                    relay = adjustedRelay
                )
            }
        }
        lastProcessTime.set(System.currentTimeMillis())
    }

    private fun startProcessingJob() {
        Log.i(TAG, "Start job")
        scope.launch {
            while (true) {
                delay(WAIT_TIME)
                if (lastProcessTime.get() <= System.currentTimeMillis() - WAIT_TIME) {
                    processNow()
                } else Log.i(TAG, "Skip job")
            }
        }.invokeOnCompletion {
            Log.w(TAG, "Processing job completed", it)
            jobTerminated.set(true)
        }
    }

    private fun batchFilters(filters: Set<Filter>): List<Filter> {
        val simpleNoteFilters = filters.filter { it.isSimpleNoteFilter() }
        val simpleProfileFilters = filters.filter { it.isSimpleProfileFilter() }

        if (simpleNoteFilters.size <= 1 && simpleProfileFilters.size <= 1) return filters.toList()

        val batchedFilters = mutableListOf<Filter>()
        if (simpleNoteFilters.size > 1) {
            val noteIds = simpleNoteFilters.flatMap { it.ids.orEmpty() }.distinct()
            batchedFilters.add(Filter.createNoteFilter(ids = noteIds))
        }
        if (simpleProfileFilters.size > 1) {
            val pubkeys = simpleProfileFilters.flatMap { it.authors.orEmpty() }.distinct()
            batchedFilters.add(Filter.createProfileFilter(pubkeys = pubkeys))
        }

        return filters
            .filter { !it.isSimpleNoteFilter() && !it.isSimpleProfileFilter() } + batchedFilters
    }

    private fun MutableMap<Relay, MutableSet<Filter>>.syncedAddOrCreate(
        relays: Collection<Relay>?,
        filter: Filter
    ) {
        synchronized(this) {
            if (relays == null) this.addOrCreate(key = "", itemToAdd = filter)
            else relays.forEach { relay -> queue.addOrCreate(relay, filter) }
        }
    }
}
