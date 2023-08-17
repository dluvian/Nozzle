package com.dluvian.nozzle.data.provider.impl

import android.util.Log
import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.provider.IAutopilotProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.Nip65Dao

private const val TAG = "AutopilotProvider"

class AutopilotProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val nip65Dao: Nip65Dao,
    private val eventRelayDao: EventRelayDao,
) : IAutopilotProvider {

    // TODO: Dismiss relays you have trouble to connecting
    // java.net.ProtocolException: Expected HTTP 101 response but was '502 Bad Gateway'
    // javax.net.ssl.SSLPeerUnverifiedException: Hostname relay.nostr.vision not verified:

    override suspend fun getAutopilotRelays(pubkeys: Set<String>): Map<String, Set<String>> {
        Log.i(TAG, "Get autopilot relays of ${pubkeys.size} pubkeys")
        if (pubkeys.isEmpty()) return emptyMap()

        val result = mutableListOf<Pair<String, Set<String>>>()
        val processedPubkeys = mutableSetOf<String>()

        // We don't use relayProvider here because it depends on AutopilotProvider
        val myReadRelays = nip65Dao
            .getReadRelaysOfPubkey(pubkey = pubkeyProvider.getPubkey())
            .ifEmpty { getDefaultRelays() }
            .toSet()

        processNip65(
            myReadRelays = myReadRelays,
            result = result,
            processedPubkeys = processedPubkeys,
            pubkeys = pubkeys
        )

        if (pubkeys.size > processedPubkeys.size) {
            processEventRelays(
                myReadRelays = myReadRelays,
                result = result,
                processedPubkeys = processedPubkeys,
                pubkeys = pubkeys.minus(processedPubkeys)
            )
        }

        if (pubkeys.size > processedPubkeys.size) {
            processFallback(
                myReadRelays = myReadRelays,
                result = result,
                processedPubkeys = processedPubkeys,
                pubkeys = pubkeys.minus(processedPubkeys)
            )
        }

        if (pubkeys.size > processedPubkeys.size) {
            Log.w(TAG, "Failed to process all pubkeys")
        }

        return mergeResult(result)
    }

    private suspend fun processNip65(
        myReadRelays: Set<String>,
        result: MutableList<Pair<String, Set<String>>>,
        processedPubkeys: MutableSet<String>,
        pubkeys: Collection<String>
    ) {
        nip65Dao.getPubkeysPerWriteRelayMap(pubkeys = pubkeys)
            .toList()
            .sortedByDescending { it.second.size }
            .sortedByDescending { myReadRelays.contains(it.first) } // Prefer my relays
            .forEach {
                val pubkeysToAdd = it.second.minus(processedPubkeys)
                if (pubkeysToAdd.isNotEmpty()) {
                    processedPubkeys.addAll(pubkeysToAdd)
                    result.add(Pair(it.first, pubkeysToAdd))
                }
            }
        Log.d(TAG, "Processed ${result.size} nip65 relays for ${processedPubkeys.size} pubkeys")
    }

    private suspend fun processEventRelays(
        myReadRelays: Set<String>,
        result: MutableList<Pair<String, Set<String>>>,
        processedPubkeys: MutableSet<String>,
        pubkeys: Collection<String>
    ) {
        val newlyProcessedPubkeys = mutableSetOf<String>()
        val newlyProcessedEventRelays = mutableMapOf<String, MutableSet<String>>()

        eventRelayDao.getCountedRelaysPerPubkey(pubkeys = pubkeys)
            .sortedByDescending { it.numOfPosts }
            .sortedByDescending { myReadRelays.contains(it.relayUrl) } // Prefer my relays
            .forEach {
                if (!newlyProcessedPubkeys.contains(it.pubkey)) {
                    newlyProcessedPubkeys.add(it.pubkey)
                    val current =
                        newlyProcessedEventRelays.putIfAbsent(it.relayUrl, mutableSetOf(it.pubkey))
                    current?.add(it.pubkey)
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

    private fun processFallback(
        myReadRelays: Set<String>,
        result: MutableList<Pair<String, Set<String>>>,
        processedPubkeys: MutableSet<String>,
        pubkeys: Set<String>
    ) {
        if (pubkeys.isEmpty() || myReadRelays.isEmpty()) return

        val chunkSize = (pubkeys.size / myReadRelays.size) + 1
        val chunkedPubkeys = pubkeys.chunked(chunkSize) { it.toSet() }

        myReadRelays.shuffled().zip(chunkedPubkeys).forEach {
            result.add(Pair(it.first, it.second))
            processedPubkeys.addAll(it.second)
        }

        Log.i(
            TAG,
            "Fall back to ${myReadRelays.size} read relays for " +
                    "${pubkeys.size} pubkeys in ${chunkedPubkeys.size} chunks"
        )
    }

    private fun mergeResult(toMerge: List<Pair<String, Set<String>>>): Map<String, Set<String>> {
        val result = mutableMapOf<String, MutableSet<String>>()
        toMerge.forEach {
            if (it.second.isNotEmpty()) {
                val current = result.putIfAbsent(it.first, it.second.toMutableSet())
                current?.let { _ -> result[it.first]?.addAll(it.second) }
            }
        }

        return result
    }
}
