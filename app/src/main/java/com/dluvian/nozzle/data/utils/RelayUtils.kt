package com.dluvian.nozzle.data.utils

import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.RelayActive

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
    val present = list.map { it.relay }.toSet()
    getMaxRelays(from = relaysUrlsToAdd, prefer = present).forEach {
        if (!present.contains(it)) {
            result.add(RelayActive(relay = it, isActive = true))
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

fun listRelayStatuses(
    allRelays: List<Relay>,
    activeRelays: List<Relay>? = null // null == all active
): List<RelayActive> {
    return allRelays.distinct().map { relay ->
        RelayActive(
            relay = relay,
            isActive = activeRelays == null || activeRelays.contains(relay)
        )
    }
}
