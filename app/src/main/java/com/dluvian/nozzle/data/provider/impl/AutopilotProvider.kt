package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.data.provider.IAutopilotProvider
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.data.utils.addOrCreate
import com.dluvian.nozzle.data.utils.getMaxRelays
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay

private const val TAG = "AutopilotProvider"

class AutopilotProvider(
    private val relayProvider: IRelayProvider,
    private val contactListProvider: IContactListProvider,
    private val nip65Dao: Nip65Dao,
    private val eventRelayDao: EventRelayDao,
    private val nozzleSubscriber: INozzleSubscriber,
) : IAutopilotProvider {

    // TODO: Dismiss relays you have trouble to connecting
    // java.net.ProtocolException: Expected HTTP 101 response but was '502 Bad Gateway'
    // javax.net.ssl.SSLPeerUnverifiedException: Hostname relay.nostr.vision not verified:

    override suspend fun getAutopilotRelays(): Map<Relay, Set<Pubkey>> {
        val pubkeys = contactListProvider.listPersonalContactPubkeysOrDefault().toSet()
        Log.i(TAG, "Get autopilot relays of ${pubkeys.size} pubkeys")
        if (pubkeys.isEmpty()) return emptyMap()

        nozzleSubscriber.subscribeNip65(pubkeys = pubkeys)

        val result = associatePubkeysInRandomChunksWithReadRelays(pubkeys = pubkeys)
        val processedPubkeys = mutableSetOf<Pubkey>()

        processNip65(
            result = result,
            processedPubkeys = processedPubkeys,
            pubkeys = pubkeys,
        )

        if (pubkeys.size > processedPubkeys.size) {
            processEventRelays(
                result = result,
                processedPubkeys = processedPubkeys,
                pubkeys = pubkeys.minus(processedPubkeys),
            )
        }

        if (pubkeys.size > processedPubkeys.size) {
            val unprocessedCount = pubkeys.size - processedPubkeys.size
            Log.i(TAG, "Failed to identify relays for $unprocessedCount of ${pubkeys.size} pubkeys")
        }

        return mergeResult(result)
    }

    private suspend fun processNip65(
        result: MutableList<Pair<String, Set<String>>>,
        processedPubkeys: MutableSet<String>,
        pubkeys: Collection<String>,
    ) {
        val mostUsedRelays = eventRelayDao.getAllSortedByNumOfEvents(limit = MAX_RELAYS).toSet()
        var count = 0
        nip65Dao.getPubkeysByWriteRelays(pubkeys = pubkeys)
            .toList()
            .shuffled()
            .sortedByDescending { (_, pubkeys) -> pubkeys.size }
            .sortedByDescending { (relay, _) -> mostUsedRelays.contains(relay) } // Prefer most used
            .forEach { (relay, pubkeys) ->
                val pubkeysToAdd = pubkeys.minus(processedPubkeys)
                if (pubkeysToAdd.isNotEmpty()) {
                    processedPubkeys.addAll(pubkeysToAdd)
                    result.add(Pair(relay, pubkeysToAdd))
                    count++
                }
            }
        Log.d(TAG, "Processed $count nip65 relays for ${processedPubkeys.size} pubkeys")
    }

    private suspend fun processEventRelays(
        result: MutableList<Pair<String, Set<String>>>,
        processedPubkeys: MutableSet<String>,
        pubkeys: Collection<String>,
    ) {
        val newlyProcessedPubkeys = mutableSetOf<String>()
        val newlyProcessedEventRelays = mutableMapOf<String, MutableSet<String>>()

        eventRelayDao.getCountedRelaysPerPubkey(pubkeys = pubkeys)
            .shuffled()
            .sortedByDescending { it.numOfPosts }
            .forEach {
                if (!newlyProcessedPubkeys.contains(it.pubkey)) {
                    newlyProcessedPubkeys.add(it.pubkey)
                    newlyProcessedEventRelays.addOrCreate(key = it.relayUrl, itemToAdd = it.pubkey)
                }
            }

        processedPubkeys.addAll(newlyProcessedPubkeys)
        result.addAll(newlyProcessedEventRelays.toList())

        Log.d(
            TAG,
            "Processed ${newlyProcessedEventRelays.size} event relays for ${
                newlyProcessedPubkeys.size
            } pubkeys"
        )
        Log.d(
            TAG,
            "Selected event relays ${newlyProcessedEventRelays.map { Pair(it.key, it.value.size) }}"
        )
    }

    private fun mergeResult(toMerge: List<Pair<Relay, Set<Pubkey>>>): Map<Relay, Set<Pubkey>> {
        val result = mutableMapOf<Relay, MutableSet<Pubkey>>()
        toMerge.forEach { (relay, pubkeys) ->
            if (pubkeys.isNotEmpty()) {
                val current = result.putIfAbsent(relay, pubkeys.toMutableSet())
                current?.addAll(pubkeys)
            }
        }
        return result
    }

    private fun associatePubkeysInRandomChunksWithReadRelays(
        pubkeys: Collection<Pubkey>
    ): MutableList<Pair<Relay, Set<String>>> {
        if (pubkeys.isEmpty()) return mutableListOf()

        val readRelays = getMaxRelays(from = relayProvider.getReadRelays())
        val chunkSize = pubkeys.size / readRelays.size + 1
        val chunkedPubkeys = pubkeys.shuffled().chunked(chunkSize) { it.toSet() }

        return readRelays.zip(chunkedPubkeys).toMutableList()
    }
}
