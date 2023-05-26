package com.dluvian.nozzle.data.provider

import com.dluvian.nostrclientkt.model.Metadata
import kotlinx.coroutines.flow.Flow

interface IPersonalProfileProvider : IPubkeyProvider {
    fun updateMetadata()
    fun getMetadata(): Flow<Metadata?>
}
