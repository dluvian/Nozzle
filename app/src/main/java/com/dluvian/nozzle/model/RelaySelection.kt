package com.dluvian.nozzle.model

sealed class RelaySelection(val selectedRelays: Collection<String>?)

data object AllRelays : RelaySelection(selectedRelays = null)

class MultipleRelays(relays: List<String>) : RelaySelection(selectedRelays = relays)

class UserSpecific(val pubkeysPerRelay: Map<String, Set<String>>) : RelaySelection(
    selectedRelays = pubkeysPerRelay.filter { it.value.isNotEmpty() }.keys
)
