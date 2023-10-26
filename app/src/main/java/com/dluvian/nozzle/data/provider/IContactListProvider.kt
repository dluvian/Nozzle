package com.dluvian.nozzle.data.provider

import kotlinx.coroutines.flow.Flow

interface IContactListProvider {
    fun listPersonalContactPubkeys(): List<String>
    fun getPersonalContactPubkeysFlow(): Flow<List<String>>
}
