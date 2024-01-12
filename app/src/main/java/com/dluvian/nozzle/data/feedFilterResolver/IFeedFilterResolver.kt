package com.dluvian.nozzle.data.feedFilterResolver

import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.feedFilter.AuthorFilter
import com.dluvian.nozzle.model.feedFilter.FeedFilter

interface IFeedFilterResolver {
    suspend fun getPubkeysByRelay(feedFilter: FeedFilter): Map<Relay, Set<Pubkey>?>
    fun getPubkeys(authorFilter: AuthorFilter): Set<Pubkey>?
}
