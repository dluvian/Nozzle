package com.dluvian.nozzle.ui.app.views.profileList

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean


const val TAG = "ProfileListViewModel"

class ProfileListViewModel(
    profileFollower: IProfileFollower,
    val profileDao: ProfileDao,
    val contactDao: ContactDao,
) : ViewModel() {
    private val _forcedFollowState: MutableStateFlow<MutableMap<Pubkey, Boolean>> =
        MutableStateFlow(mutableMapOf())
    val forcedFollowState = _forcedFollowState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        mapOf()
    )

    var profileList: StateFlow<ProfileList?> = MutableStateFlow(null)

    val onSetProfileList: (Pubkey, ProfileListType) -> Unit = local@{ pubkey, type ->
        setProfileList(pubkey = pubkey, type = type)
    }

    private val isSettingList = AtomicBoolean(false)

    private fun setProfileList(pubkey: Pubkey, type: ProfileListType) {
        if (!isSettingList.compareAndSet(false, true)) return
        profileList = MutableStateFlow(null)
        viewModelScope.launch(Dispatchers.IO) {
            val pubkeys = when (type) {
                ProfileListType.FOLLOWER_LIST -> contactDao.listContactPubkeys(pubkey = pubkey)
                ProfileListType.FOLLOWED_BY_LIST -> contactDao.listFollowedByPubkeys(pubkey = pubkey)
            }

            profileList = profileDao
                .getSimpleProfilesFlow(pubkeys = pubkeys, contactDao = contactDao)
                .map { ProfileList(profiles = it, type = type) }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)
        }.invokeOnCompletion {
            isSettingList.set(false)
            if (it != null) Log.w(TAG, "Failed to set profile list pubkey=$pubkey, type=$type")
        }
    }

    private var followProcesses: MutableMap<Pubkey, Job?> = mutableMapOf()
    val onFollow: (Int) -> Unit = local@{ indexToFollow ->
        val pubkey = profileList.value?.profiles?.get(indexToFollow)?.pubkey ?: return@local
        if (_forcedFollowState.value[pubkey] == true) return@local

        followProcesses[pubkey]?.cancel(CancellationException("Cancelled to start follow process"))
        _forcedFollowState.update { it.apply { it[pubkey] = true } }
        followProcesses[pubkey] = viewModelScope.launch(context = Dispatchers.IO) {
            profileFollower.follow(pubkeyToFollow = pubkey)
        }
        followProcesses[pubkey]?.invokeOnCompletion { ex ->
            Log.i(TAG, "Follow process completed: error=${ex?.localizedMessage}")
        }
    }

    val onUnfollow: (Int) -> Unit = local@{ indexToUnfollow ->
        val pubkey = profileList.value?.profiles?.get(indexToUnfollow)?.pubkey ?: return@local
        if (_forcedFollowState.value[pubkey] == false) return@local

        followProcesses[pubkey]?.cancel(
            CancellationException("Cancelled to start unfollow process")
        )
        _forcedFollowState.update { it.apply { it[pubkey] = false } }
        followProcesses[pubkey] = viewModelScope.launch(context = Dispatchers.IO) {
            profileFollower.unfollow(pubkeyToUnfollow = pubkey)
        }
        followProcesses[pubkey]?.invokeOnCompletion { ex ->
            Log.i(TAG, "Unfollow process completed: error=${ex?.localizedMessage}")
        }
    }

    companion object {
        fun provideFactory(
            profileFollower: IProfileFollower,
            profileDao: ProfileDao,
            contactDao: ContactDao,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileListViewModel(
                        profileFollower = profileFollower,
                        profileDao = profileDao,
                        contactDao = contactDao
                    ) as T
                }
            }
    }
}
