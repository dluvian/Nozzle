package com.dluvian.nozzle.data.profileFollower

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.entity.ContactEntity
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException

private const val TAG = "ProfileFollower"

class ProfileFollower(
    private val nostrService: INostrService,
    private val pubkeyProvider: IPubkeyProvider,
    private val relayProvider: IRelayProvider,
    private val contactDao: ContactDao,
) : IProfileFollower {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val followProcesses: MutableMap<Pubkey, Job> = mutableMapOf()

    private val forcedFollowState = mutableStateOf(emptyMap<Pubkey, Boolean>())

    val pubkeyState = pubkeyProvider.getActivePubkeyStateFlow()
        .onEach local@{ forcedFollowState.value = emptyMap() }
        .stateIn(scope, SharingStarted.Eagerly, "")

    override fun follow(pubkeyToFollow: Pubkey) {
        putForcedFollowState(pubkey = pubkeyToFollow, isFollowed = true)

        synchronized(followProcesses) {
            followProcesses[pubkeyToFollow]?.cancel(CancellationException("Cancel to start follow process"))
            followProcesses[pubkeyToFollow] = scope.launch(Dispatchers.IO) {
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
                if (it != null) putForcedFollowState(pubkey = pubkeyToFollow, isFollowed = false)
            }
        }
    }

    override fun unfollow(pubkeyToUnfollow: Pubkey) {
        putForcedFollowState(pubkey = pubkeyToUnfollow, isFollowed = false)

        synchronized(followProcesses) {
            followProcesses[pubkeyToUnfollow]?.cancel(CancellationException("Cancel to start unfollow process"))
            followProcesses[pubkeyToUnfollow] = scope.launch(Dispatchers.IO) {
                contactDao.deleteContact(
                    pubkey = pubkeyProvider.getActivePubkey(), contactPubkey = pubkeyToUnfollow
                )
                updateContactList(personalPubkey = pubkeyProvider.getActivePubkey())
            }
            followProcesses[pubkeyToUnfollow]?.invokeOnCompletion {
                Log.i(TAG, "Completed unfollow process. Error = ${it?.localizedMessage}")
                if (it != null) putForcedFollowState(pubkey = pubkeyToUnfollow, isFollowed = true)
            }
        }
    }

    override fun getForceFollowedState(): State<Map<Pubkey, Boolean>> {
        return forcedFollowState
    }

    private fun putForcedFollowState(pubkey: Pubkey, isFollowed: Boolean) {
        forcedFollowState.value = forcedFollowState.value
            .toMutableMap()
            .apply { this[pubkey] = isFollowed }
    }

    private suspend fun updateContactList(personalPubkey: String) {
        val contactPubkeys = contactDao.listContactPubkeys(pubkey = personalPubkey)
        val event = nostrService.updateContactList(
            contactPubkeys = contactPubkeys, relays = relayProvider.getWriteRelays()
        )
        contactDao.updateTime(pubkey = event.pubkey, createdAt = event.createdAt)
    }
}
