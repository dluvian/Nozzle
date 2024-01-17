package com.dluvian.nozzle.data.feedFilterResolver

import com.dluvian.nozzle.data.provider.IAutopilotProvider
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.feedFilter.AuthorFilter
import com.dluvian.nozzle.model.feedFilter.Autopilot
import com.dluvian.nozzle.model.feedFilter.FeedFilter
import com.dluvian.nozzle.model.feedFilter.FriendCircle
import com.dluvian.nozzle.model.feedFilter.Friends
import com.dluvian.nozzle.model.feedFilter.Global
import com.dluvian.nozzle.model.feedFilter.MultipleRelays
import com.dluvian.nozzle.model.feedFilter.ReadRelays
import com.dluvian.nozzle.model.feedFilter.SingularPerson

class FeedFilterResolver(
    private val autopilotProvider: IAutopilotProvider,
    private val relayProvider: IRelayProvider,
    private val contactListProvider: IContactListProvider
) : IFeedFilterResolver {
    override suspend fun getPubkeysByRelay(feedFilter: FeedFilter): Map<Relay, Set<Pubkey>?> {
        val pubkeys = getPubkeys(authorFilter = feedFilter.authorFilter)
        return when (feedFilter.relayFilter) {
            is Autopilot -> autopilotProvider.getAutopilotRelays(authorFilter = feedFilter.authorFilter)
            is ReadRelays -> relayProvider.getReadRelays(limit = true).associateWith { pubkeys }
            is MultipleRelays -> feedFilter.relayFilter.relays.associateWith { pubkeys }
        }
    }

    override fun getPubkeys(authorFilter: AuthorFilter): Set<Pubkey>? {
        return when (authorFilter) {
            is Global -> null
            is Friends -> contactListProvider.listPersonalContactPubkeysOrDefault().toSet()
            is FriendCircle -> contactListProvider.listFriendCirclePubkeysOrDefault()
            is SingularPerson -> setOf(authorFilter.pubkey)
        }
    }
}
