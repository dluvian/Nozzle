package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.ItemWithOnlineStatus
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.relay.RelayProfile
import kotlinx.coroutines.flow.Flow

interface IRelayProfileProvider {
    suspend fun getRelayProfile(relayUrl: Relay): Flow<ItemWithOnlineStatus<RelayProfile?>>
    suspend fun update(relayUrl: Relay)
}
