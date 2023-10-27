package com.dluvian.nozzle.data.manager.impl

import com.dluvian.nozzle.data.manager.IPersonalProfileManager
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class PersonalProfileManager(
    private val pubkeyProvider: IPubkeyProvider,
    private val relayProvider: IRelayProvider,
    private val nostrService: INostrService,
    private val profileDao: ProfileDao
) : IPersonalProfileManager {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val metadataStateFlow = profileDao.getActiveMetadata()
        .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
        .stateIn(
            scope,
            SharingStarted.Eagerly,
            null
        )

    override suspend fun upsertMetadata(metadata: Metadata) {
        val event = nostrService.publishProfile(
            metadata = metadata,
            relays = relayProvider.getWriteRelays()
        )
        val profileEntity = ProfileEntity(
            pubkey = pubkeyProvider.getActivePubkey(),
            metadata = metadata,
            createdAt = event.createdAt
        )
        profileDao.upsertProfile(profileEntity)
    }

    override fun getMetadataStateFlow(): StateFlow<Metadata?> {
        return metadataStateFlow
    }
}
