package com.dluvian.nozzle.data.provider

interface IContactListProvider {
    suspend fun listPersonalContactPubkeys(): List<String>
}
