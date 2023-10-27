package com.dluvian.nozzle.data.manager

import com.dluvian.nozzle.data.provider.IPersonalProfileProvider
import com.dluvian.nozzle.model.nostr.Metadata

interface IPersonalProfileManager : IPersonalProfileProvider {
    suspend fun upsertMetadata(metadata: Metadata)
}
