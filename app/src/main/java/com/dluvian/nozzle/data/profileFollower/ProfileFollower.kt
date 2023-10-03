package com.dluvian.nozzle.data.profileFollower

import android.util.Log
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.entity.ContactEntity

private const val TAG = "ProfileFollower"

class ProfileFollower(
    private val nostrService: INostrService,
    private val pubkeyProvider: IPubkeyProvider,
    private val relayProvider: IRelayProvider,
    private val contactDao: ContactDao,
) : IProfileFollower {

    override suspend fun follow(pubkeyToFollow: String) {
        Log.i(TAG, "Follow $pubkeyToFollow")
        contactDao.insertOrIgnore(
            ContactEntity(
                pubkey = pubkeyProvider.getPubkey(),
                contactPubkey = pubkeyToFollow,
                createdAt = 0
            )
        )
        updateContactList(personalPubkey = pubkeyProvider.getPubkey())
    }

    override suspend fun unfollow(pubkeyToUnfollow: String) {
        Log.i(TAG, "Unfollow $pubkeyToUnfollow")
        contactDao.deleteContact(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkey = pubkeyToUnfollow
        )
        updateContactList(personalPubkey = pubkeyProvider.getPubkey())
    }

    private suspend fun updateContactList(personalPubkey: String) {
        val contactPubkeys = contactDao.listContactPubkeys(pubkey = personalPubkey)
        val event = nostrService.updateContactList(
            contactPubkeys = contactPubkeys,
            relays = relayProvider.getWriteRelays()
        )
        contactDao.updateTime(pubkey = event.pubkey, createdAt = event.createdAt)
    }
}
