package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay

interface IAutopilotProvider {
    suspend fun getAutopilotRelays(): Map<Relay, Set<Pubkey>>
}
