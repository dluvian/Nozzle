package com.dluvian.nozzle.data.profileFollower

import android.util.Log
import androidx.compose.runtime.State
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.entity.ContactEntity
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Collections
import java.util.concurrent.CancellationException

private const val TAG = "ProfileFollower"

class ProfileFollower(
    private val nostrService: INostrService,
    private val pubkeyProvider: IPubkeyProvider,
    private val relayProvider: IRelayProvider,
    private val contactDao: ContactDao,
) : IProfileFollower {
    private val followProcesses: MutableMap<Pubkey, Job> =
        Collections.synchronizedMap(mutableMapOf())

    override fun follow(scope: CoroutineScope, pubkeyToFollow: Pubkey) {
        followProcesses[pubkeyToFollow]?.cancel(CancellationException("Cancel to start follow process"))
        followProcesses[pubkeyToFollow] = scope.launch(Dispatchers.IO) {
            Log.i(TAG, "Follow $pubkeyToFollow")
            contactDao.insertOrIgnore(
                ContactEntity(
                    pubkey = pubkeyProvider.getActivePubkey(),
                    contactPubkey = pubkeyToFollow,
                    createdAt = 0
                )
            )
            updateContactList(personalPubkey = pubkeyProvider.getActivePubkey())
        }
        followProcesses[pubkeyToFollow]?.invokeOnCompletion {
            Log.i(TAG, "Completed follow process. Error = ${it?.localizedMessage}")
        }
    }

    override fun unfollow(scope: CoroutineScope, pubkeyToUnfollow: Pubkey) {
        followProcesses[pubkeyToUnfollow]?.cancel(CancellationException("Cancel to start unfollow process"))
        followProcesses[pubkeyToUnfollow] = scope.launch(Dispatchers.IO) {
            Log.i(TAG, "Unfollow $pubkeyToUnfollow")
            contactDao.deleteContact(
                pubkey = pubkeyProvider.getActivePubkey(),
                contactPubkey = pubkeyToUnfollow
            )
            updateContactList(personalPubkey = pubkeyProvider.getActivePubkey())
        }
        followProcesses[pubkeyToUnfollow]?.invokeOnCompletion {
            Log.i(TAG, "Completed unfollow process. Error = ${it?.localizedMessage}")
        }
    }

    override fun getIsFollowedByMeState(pubkey: Pubkey): State<Boolean> {
        TODO("Not yet implemented")
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
