package com.dluvian.nozzle.ui.app.views.profileList

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean


const val TAG = "ProfileListViewModel"

class ProfileListViewModel(
    profileFollower: IProfileFollower,
    private val simpleProfileProvider: ISimpleProfileProvider,
    private val nozzleSubscriber: INozzleSubscriber,
    val contactDao: ContactDao,
) : ViewModel() {
    private val _forcedFollowState: MutableStateFlow<MutableMap<Pubkey, Boolean>> =
        MutableStateFlow(mutableMapOf())
    val forcedFollowState = _forcedFollowState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        mapOf()
    )

    var profileList: StateFlow<ProfileList> = MutableStateFlow(ProfileList())

    private val isSettingList = AtomicBoolean(false)

    val onSetFollowerList: (Pubkey) -> Unit = local@{ pubkey ->
        viewModelScope.launch(Dispatchers.IO) {
            setProfileList(pubkey = pubkey, type = ProfileListType.FOLLOWER_LIST)
        }.invokeOnCompletion {
            isSettingList.set(false)
            if (it != null) Log.w(TAG, "Failed to set follower list of $pubkey")
        }
    }

    val onSetFollowedByList: (Pubkey) -> Unit = local@{ pubkey ->
        viewModelScope.launch(Dispatchers.IO) {
            setProfileList(pubkey = pubkey, type = ProfileListType.FOLLOWED_BY_LIST)
        }.invokeOnCompletion {
            isSettingList.set(false)
            if (it != null) Log.w(TAG, "Failed to set followed by list of $pubkey")
        }
    }


    // The IO coroutine needs to be started in the lambda itself?
    // It's not working when doing it in the function below.
    private suspend fun setProfileList(pubkey: Pubkey, type: ProfileListType) {
        if (!isSettingList.compareAndSet(false, true)) return
        val currentValue = profileList.value
        if (currentValue.pubkey == pubkey && currentValue.type == type) return

        profileList = MutableStateFlow(ProfileList(type = type))
        val pubkeys = when (type) {
            ProfileListType.FOLLOWER_LIST -> contactDao.listContactPubkeys(pubkey = pubkey)
            ProfileListType.FOLLOWED_BY_LIST -> contactDao.listFollowedByPubkeys(pubkey = pubkey)
        }

        profileList = simpleProfileProvider
            .getSimpleProfilesFlow(pubkeys = pubkeys)
            .distinctUntilChanged()
            .map { ProfileList(pubkey = pubkey, profiles = it, type = type) }
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                ProfileList(pubkey = pubkey, type = type)
            )
    }

    private var followProcesses: MutableMap<Pubkey, Job?> = mutableMapOf()
    val onFollow: (Int) -> Unit = local@{ indexToFollow ->
        val pubkey = profileList.value.profiles[indexToFollow].pubkey
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
        val pubkey = profileList.value.profiles[indexToUnfollow].pubkey
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

    private var lastOwnerPubkey: Pubkey = ""
    val onSubscribeToUnknowns: (Pubkey) -> Unit = local@{
        synchronized(lastOwnerPubkey) {
            if (it == lastOwnerPubkey) return@local
            lastOwnerPubkey = it
        }
        val unknownPubkeys = profileList.value
            .profiles
            .filter { it.name.isEmpty() }
            .map { it.pubkey }
        viewModelScope.launch(Dispatchers.IO) {
            nozzleSubscriber.subscribeSimpleProfiles(pubkeys = unknownPubkeys)
        }
    }

    companion object {
        fun provideFactory(
            profileFollower: IProfileFollower,
            simpleProfileProvider: ISimpleProfileProvider,
            nozzleSubscriber: INozzleSubscriber,
            contactDao: ContactDao,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileListViewModel(
                        profileFollower = profileFollower,
                        simpleProfileProvider = simpleProfileProvider,
                        nozzleSubscriber = nozzleSubscriber,
                        contactDao = contactDao
                    ) as T
                }
            }
    }
}
