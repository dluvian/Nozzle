package com.dluvian.nozzle.data.provider


interface IRelayProvider {
    fun getReadRelays(): List<String>
    fun getWriteRelays(): List<String>
    suspend fun getAutopilotRelays(): Map<String, Set<String>>
}
