package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.provider.IRelayProfileProvider
import com.dluvian.nozzle.model.ItemWithOnlineStatus
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.Waiting
import com.dluvian.nozzle.model.relay.RelayProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RelayProfileProvider : IRelayProfileProvider {
    override suspend fun getRelayProfile(relayUrl: Relay): Flow<ItemWithOnlineStatus<RelayProfile?>> {
        return flowOf(ItemWithOnlineStatus(item = RelayProfile(), onlineStatus = Waiting))
    }

    override suspend fun update(relayUrl: Relay) {
        delay(WAIT_TIME)
    }
}