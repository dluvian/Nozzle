package com.dluvian.nozzle.model

sealed class RelaySelection {
    abstract fun getSelectedRelays(): Collection<String>?
}

object AllRelays : RelaySelection() {
    override fun getSelectedRelays(): List<String>? = null
}

class MultipleRelays(private val relays: List<String>) : RelaySelection() {
    override fun getSelectedRelays(): List<String> = relays
}

class UserSpecific(val pubkeysPerRelay: Map<String, Set<String>>) : RelaySelection() {
    override fun getSelectedRelays(): Set<String> {
        return pubkeysPerRelay.filter { it.value.isNotEmpty() }.keys
    }
}
