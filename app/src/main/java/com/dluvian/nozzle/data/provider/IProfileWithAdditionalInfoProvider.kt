package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.ProfileWithAdditionalInfo
import kotlinx.coroutines.flow.Flow

interface IProfileWithAdditionalInfoProvider {
    fun getProfileFlow(pubkey: String): Flow<ProfileWithAdditionalInfo>
}
