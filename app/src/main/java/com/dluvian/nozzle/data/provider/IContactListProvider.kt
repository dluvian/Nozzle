package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.data.getDefaultPubkeys
import kotlinx.coroutines.flow.Flow

interface IContactListProvider {
    fun listPersonalContactPubkeys(): List<String>

    fun listPersonalContactPubkeysOrDefault() = listPersonalContactPubkeys()
        .ifEmpty { getDefaultPubkeys() }

    fun getPersonalContactPubkeysFlow(): Flow<List<String>>
}
