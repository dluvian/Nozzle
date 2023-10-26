package com.dluvian.nozzle.data.manager.impl

import com.dluvian.nozzle.data.manager.IPersonalProfileManager
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ProfileDao
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

    override suspend fun setMeta(
        name: String,
        about: String,
        picture: String,
        nip05: String,
        lud16: String
    ) {
        profileDao.updateMetadata(
            pubkey = getActivePubkey(),
            name = name,
            about = about,
            picture = picture,
            nip05 = nip05,
            lud16 = lud16,
        )
    }


    override fun getMetadataStateFlow(): StateFlow<Metadata?> {
        return metadataStateFlow
    }

    override fun getActivePubkey() = pubkeyProvider.getActivePubkey()

    override fun getActiveNpub() = pubkeyProvider.getActiveNpub()
}
