package com.dluvian.nozzle.data.profileFollower

import android.util.Log
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.entity.ContactEntity
import com.dluvian.nozzle.model.nostr.Event

private const val TAG = "ProfileFollower"

class ProfileFollower(
    private val nostrService: INostrService,
    private val pubkeyProvider: IPubkeyProvider,
    private val contactDao: ContactDao,
) : IProfileFollower {

    override suspend fun follow(pubkeyToFollow: String, relayUrl: String) {
        Log.i(TAG, "Follow $pubkeyToFollow")
        contactDao.insertOrIgnore(
            ContactEntity(
                pubkey = pubkeyProvider.getPubkey(),
                contactPubkey = pubkeyToFollow,
                relayUrl = relayUrl,
                createdAt = 0
            )
        )
        val contacts = contactDao.listContacts(pubkey = pubkeyProvider.getPubkey())
        val event = updateContactListViaNostr(contacts)
        contactDao.updateTime(pubkey = pubkeyProvider.getPubkey(), createdAt = event.createdAt)
    }

    override suspend fun unfollow(pubkeyToUnfollow: String) {
        Log.i(TAG, "Unfollow $pubkeyToUnfollow")
        contactDao.deleteContact(
            pubkey = pubkeyProvider.getPubkey(),
            contactPubkey = pubkeyToUnfollow
        )
        val contacts = contactDao.listContacts(pubkey = pubkeyProvider.getPubkey())
        val event = updateContactListViaNostr(contacts)
        contactDao.updateTime(pubkey = pubkeyProvider.getPubkey(), createdAt = event.createdAt)
    }

    private fun updateContactListViaNostr(contactEntities: List<ContactEntity>): Event {
        return nostrService.updateContactList(
            contacts = contactEntities.map { it.toContactListEntry() }
        )
    }

}
