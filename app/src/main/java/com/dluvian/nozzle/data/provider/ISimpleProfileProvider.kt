package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.WaitTime
import com.dluvian.nozzle.ui.app.views.profileList.ProfileListType
import kotlinx.coroutines.flow.Flow

interface ISimpleProfileProvider {
    suspend fun getSimpleProfilesFlow(
        type: ProfileListType,
        pubkey: Pubkey,
        underPubkey: Pubkey,
        limit: Int,
        waitForSubscription: WaitTime
    ): Flow<List<SimpleProfile>>

    suspend fun getSimpleProfilesFlow(nameLike: String): Flow<List<SimpleProfile>>
}
