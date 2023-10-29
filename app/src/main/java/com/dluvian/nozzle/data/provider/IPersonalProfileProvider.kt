package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.flow.StateFlow

interface IPersonalProfileProvider {
    fun getMetadataStateFlow(): StateFlow<Metadata?>
}
