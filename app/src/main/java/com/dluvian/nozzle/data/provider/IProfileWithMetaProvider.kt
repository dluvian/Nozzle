package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.ProfileWithMeta
import kotlinx.coroutines.flow.Flow

interface IProfileWithMetaProvider {
    suspend fun getProfileFlow(profileId: String): Flow<ProfileWithMeta>
}
