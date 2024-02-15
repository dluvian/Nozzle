package com.dluvian.nozzle.data.utils

import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.relay.RelaySelection

fun toggleRelay(relays: List<RelaySelection>, index: Int): List<RelaySelection> {
    return relays.mapIndexed { i, relay ->
        if (index == i) relay.copy(isActive = !relay.isActive) else relay
    }
}

fun addLimitedRelayStatuses(
    list: List<RelaySelection>,
    relaysUrlsToAdd: List<String>
): List<RelaySelection> {
    val result = list.toMutableList()
    val present = list.map { it.relay }.toSet()
    getMaxRelays(from = relaysUrlsToAdd, prefer = present).forEach {
        if (!present.contains(it)) {
            result.add(RelaySelection(relay = it, isActive = true))
        }
    }

    return result
}

fun getMaxRelays(from: List<Relay>, prefer: Collection<Relay> = emptyList()): List<Relay> {
    return from.shuffled()
        .sortedByDescending { prefer.contains(it) }
        .take(MAX_RELAYS)
}

fun getMaxRelaysAndAddIfTooSmall(
    from: List<Relay>,
    prefer: Collection<Relay> = emptyList()
): List<Relay> {
    val result = getMaxRelays(from = from, prefer = prefer)

    return if (result.size >= MAX_RELAYS) result
    else (result + prefer.shuffled()).distinct().take(MAX_RELAYS)
}

fun listRelaySelection(
    allRelays: List<Relay>,
    selectedRelays: List<Relay>? = null // null == all selected
): List<RelaySelection> {
    return allRelays.distinct().map { relay ->
        RelaySelection(
            relay = relay,
            isActive = selectedRelays == null || selectedRelays.contains(relay)
        )
    }
}
