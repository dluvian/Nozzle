package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.data.getDefaultPubkeys
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.flow.Flow

interface IContactListProvider {
    fun listPersonalContactPubkeys(): List<String>

    fun listPersonalContactPubkeysOrDefault() = listPersonalContactPubkeys()
        .ifEmpty { getDefaultPubkeys() }

    fun getPersonalContactPubkeysFlow(): Flow<List<String>>

    fun listFriendCirclePubkeys(): Set<Pubkey>
    fun listFriendCirclePubkeysOrDefault(): Set<Pubkey> {
        return listFriendCirclePubkeys().ifEmpty { getDefaultPubkeys().toSet() }
    }
}
