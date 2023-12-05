package com.dluvian.nozzle.data.utils

import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.MultipleRelays
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.RelaySelection
import com.dluvian.nozzle.model.UserSpecific

fun toggleRelay(relays: List<RelayActive>, index: Int): List<RelayActive> {
    return relays.mapIndexed { i, relay ->
        if (index == i) relay.copy(isActive = !relay.isActive) else relay
    }
}

fun addLimitedRelayStatuses(
    list: List<RelayActive>,
    relaysUrlsToAdd: List<String>
): List<RelayActive> {
    val result = list.toMutableList()
    val present = list.map { it.relayUrl }.toSet()
    relaysUrlsToAdd
        .shuffled()
        .sortedByDescending { present.contains(it) }
        .take(MAX_RELAYS)
        .forEach {
            if (!present.contains(it)) {
                result.add(RelayActive(relayUrl = it, isActive = true))
            }
        }

    return result
}

fun listRelayStatuses(
    allRelayUrls: List<String>,
    relaySelection: RelaySelection
): List<RelayActive> {
    return allRelayUrls.distinct().map {
        var count = 0
        val isActive = when (relaySelection) {
            is AllRelays -> true
            is MultipleRelays -> relaySelection.selectedRelays?.contains(it) ?: false
            is UserSpecific -> {
                count = relaySelection.pubkeysPerRelay[it].orEmpty().size
                relaySelection.selectedRelays?.contains(it) ?: false
            }
        }
        RelayActive(relayUrl = it, isActive = isActive, count = count)
    }
}
