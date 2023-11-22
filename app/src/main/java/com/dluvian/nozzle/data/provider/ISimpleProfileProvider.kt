package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import kotlinx.coroutines.flow.Flow

interface ISimpleProfileProvider {
    fun getSimpleProfilesFlow(pubkeys: Collection<Pubkey>): Flow<List<SimpleProfile>>

    suspend fun getSimpleProfilesFlow(nameLike: String): Flow<List<SimpleProfile>>
}
