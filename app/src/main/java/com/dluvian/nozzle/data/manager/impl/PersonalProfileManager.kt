package com.dluvian.nozzle.data.manager.impl

import android.util.Log
import com.dluvian.nozzle.data.manager.IPersonalProfileManager
import com.dluvian.nozzle.data.nostr.client.model.Metadata
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ProfileDao
import kotlinx.coroutines.flow.Flow

private const val TAG = "PersonalProfileManager"

class PersonalProfileManager(
    private val pubkeyProvider: IPubkeyProvider,
    private val profileDao: ProfileDao
) : IPersonalProfileManager {
    private var metadataFlow = profileDao.getMetadata(pubkeyProvider.getPubkey())

    override suspend fun setMeta(
        name: String,
        about: String,
        picture: String,
        nip05: String,
        lud16: String
    ) {
        profileDao.updateMetadata(
            pubkey = getPubkey(),
            name = name,
            about = about,
            picture = picture,
            nip05 = nip05,
            lud16 = lud16,
        )
    }

    override fun updateMetadata() {
        Log.i(TAG, "Update metadata with new pubkey ${pubkeyProvider.getPubkey()}")
        metadataFlow = profileDao.getMetadata(pubkeyProvider.getPubkey())
    }

    override fun getMetadata(): Flow<Metadata?> {
        return metadataFlow
    }

    override fun getPubkey() = pubkeyProvider.getPubkey()

    override fun getNpub() = pubkeyProvider.getNpub()
}
