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

    override suspend fun getAutopilotRelays(pubkeys: Set<String>): Map<String, Set<String>> {
        Log.i(TAG, "Get autopilot relays of ${pubkeys.size} pubkeys")
        if (pubkeys.isEmpty()) return emptyMap()

        val result = mutableListOf<Pair<String, Set<String>>>()
        val processedPubkeys = mutableSetOf<String>()

        processNip65(result = result, processedPubkeys = processedPubkeys, pubkeys = pubkeys)

        if (pubkeys.size > processedPubkeys.size) {
            processEventRelays(
                result = result,
                processedPubkeys = processedPubkeys,
                pubkeys = pubkeys.minus(processedPubkeys)
            )
        }

        if (pubkeys.size > processedPubkeys.size) {
            processDefault(
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
        result: MutableList<Pair<String, Set<String>>>,
        processedPubkeys: MutableSet<String>,
        pubkeys: Collection<String>
    ) {
        nip65Dao.getPubkeysPerWriteRelayMap(pubkeys = pubkeys)
            .toList()
            .sortedByDescending { it.second.size }
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
        result: MutableList<Pair<String, Set<String>>>,
        processedPubkeys: MutableSet<String>,
        pubkeys: Collection<String>
    ) {
        val newlyProcessedPubkeys = mutableSetOf<String>()
        val newlyProcessedEventRelays = mutableMapOf<String, MutableSet<String>>()

        eventRelayDao.getCountedRelaysPerPubkey(pubkeys = pubkeys)
            .sortedByDescending { it.numOfPosts }
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

    private suspend fun processDefault(
        result: MutableList<Pair<String, Set<String>>>,
        processedPubkeys: MutableSet<String>,
        pubkeys: Collection<String>
    ) {
        Log.d(TAG, "Default to your read relays for ${pubkeys.size} pubkeys")

        // We don't use relayProvider here because it depends on AutopilotProvider
        nip65Dao.getReadRelaysOfPubkey(pubkey = pubkeyProvider.getPubkey())
            .ifEmpty { getDefaultRelays() }
            .randomOrNull()
            ?.let {
                Log.d(TAG, "Selected default relay $it")
                result.add(Pair(it, pubkeys.toSet()))
                processedPubkeys.addAll(pubkeys)
            }
    }

    private fun mergeResult(toMerge: List<Pair<String, Set<String>>>): Map<String, Set<String>> {
        val result = mutableMapOf<String, MutableSet<String>>()
        toMerge.forEach {
            if (it.second.isNotEmpty()) {
                val current = result.putIfAbsent(it.first, it.second.toMutableSet())
                current?.let { _ -> result[it.first]?.addAll(it.second) }
            }
        }
        Log.d(TAG, "${result.map { "${it.key} -> ${it.value.size}\n" }}")

        return result
    }
}
