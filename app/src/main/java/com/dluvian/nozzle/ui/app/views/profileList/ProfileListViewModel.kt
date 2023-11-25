package com.dluvian.nozzle.ui.app.views.profileList

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.MAX_LIST_LENGTH
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger


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

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    private val paginator: IPaginator<SimpleProfile, Pubkey> = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _isRefreshing.update { bool } },
        onGetPage = { lastPubkey, waitForSubscription ->
            simpleProfileProvider.getSimpleProfilesFlow(
                type = type.value,
                pubkey = pubkey.value,
                underPubkey = lastPubkey,
                limit = MAX_LIST_LENGTH,
                waitForSubscription = waitForSubscription
            )
        },
        onIdentifyLastParam = { profile -> profile?.pubkey.orEmpty() }
    )

    val profiles: StateFlow<StateFlow<List<SimpleProfile>>> = paginator.getList()
    val type: MutableState<ProfileListType> = mutableStateOf(ProfileListType.FOLLOWER_LIST)
    val pubkey: MutableState<Pubkey> = mutableStateOf("")

    val onSetFollowerList: (Pubkey) -> Unit = {
        pubkey.value = it
        type.value = ProfileListType.FOLLOWER_LIST
        paginator.reset()
        lastPubkeyToLoadMore.value = ""
    }

    val onSetFollowedByList: (Pubkey) -> Unit = {
        pubkey.value = it
        type.value = ProfileListType.FOLLOWED_BY_LIST
        paginator.reset()
        lastPubkeyToLoadMore.value = ""
    }

    private val lastPubkeyToLoadMore = mutableStateOf("")
    val onLoadMore: () -> Unit = {
        val lastPubkey = profiles.value.value.lastOrNull()?.pubkey.orEmpty()
        if (lastPubkeyToLoadMore.value != lastPubkey) {
            paginator.loadMore()
            lastPubkeyToLoadMore.value = lastPubkey
        }
    }

    private var followProcesses: MutableMap<Pubkey, Job?> = mutableMapOf()
    val onFollow: (Int) -> Unit = local@{ indexToFollow ->
        val pubkey = profiles.value.value[indexToFollow].pubkey
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
        val pubkey = profiles.value.value[indexToUnfollow].pubkey
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

    private val lastSize = AtomicInteger(0)
    val onSubscribeToUnknowns: (Pubkey) -> Unit = local@{
        if (lastSize.get() == profiles.value.value.size) return@local

        lastSize.set(profiles.value.value.size)
        val unknownPubkeys = profiles.value.value
            .filter { it.name.isEmpty() }
            .map { it.pubkey }
        if (unknownPubkeys.isEmpty()) return@local

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
