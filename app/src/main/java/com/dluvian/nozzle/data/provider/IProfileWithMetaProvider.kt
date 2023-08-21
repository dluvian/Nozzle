package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.ProfileWithMeta
import kotlinx.coroutines.flow.Flow

interface IProfileWithMetaProvider {
    fun getProfileFlow(pubkey: String): Flow<ProfileWithMeta>
}
