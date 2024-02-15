package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.OnlineStatus
import com.dluvian.nozzle.model.Relay
import kotlinx.coroutines.flow.Flow

interface IOnlineStatusProvider {
    fun getOnlineStatuses(relays: Collection<Relay>): Flow<Map<Relay, OnlineStatus>>
}
