package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.feedFilter.AuthorFilter
import com.dluvian.nozzle.model.feedFilter.Friends

interface IAutopilotProvider {
    suspend fun getAutopilotRelays(authorFilter: AuthorFilter = Friends): Map<Relay, Set<Pubkey>>
}
