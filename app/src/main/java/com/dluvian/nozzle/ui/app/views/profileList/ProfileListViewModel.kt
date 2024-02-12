package com.dluvian.nozzle.ui.app.views.profileList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.MAX_LIST_LENGTH
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import com.dluvian.nozzle.model.ListAndNumberFlow
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger


const val TAG = "ProfileListViewModel"

class ProfileListViewModel(
    private val simpleProfileProvider: ISimpleProfileProvider,
    private val nozzleSubscriber: INozzleSubscriber,
    val contactDao: ContactDao,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileListViewModelState())
    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            _uiState.value
        )

    private val paginator: IPaginator<SimpleProfile, Pubkey> = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _uiState.update { it.copy(isRefreshing = bool) } },
        onGetPage = { lastPubkey, waitForSubscription ->
            ListAndNumberFlow(
                listFlow = simpleProfileProvider.getSimpleProfilesFlow(
                    type = _uiState.value.type,
                    pubkey = _uiState.value.pubkey,
                    underPubkey = lastPubkey,
                    limit = MAX_LIST_LENGTH,
                    waitForSubscription = waitForSubscription
                )
            )
        },
        onIdentifyLastParam = { profile -> profile?.pubkey.orEmpty() }
    )

    val profiles: StateFlow<StateFlow<List<SimpleProfile>>> = paginator.getList()

    val onSetFollowerList: (Pubkey) -> Unit = local@{ pubkey ->
        val current = _uiState.getAndUpdate {
            it.copy(pubkey = pubkey, type = ProfileListType.FOLLOWER_LIST)
        }
        val isSame = current.let { it.pubkey == pubkey && it.type == ProfileListType.FOLLOWER_LIST }
        if (!isSame) paginator.refresh(waitForSubscription = true, useInitialValue = false)
    }

    val onSetFollowedByList: (Pubkey) -> Unit = local@{ pubkey ->
        val current = _uiState.getAndUpdate {
            it.copy(pubkey = pubkey, type = ProfileListType.FOLLOWED_BY_LIST)
        }
        val isSame =
            current.let { it.pubkey == pubkey && it.type == ProfileListType.FOLLOWED_BY_LIST }
        if (!isSame) paginator.refresh(waitForSubscription = true, useInitialValue = false)
    }

    val onLoadMore: () -> Unit = {
        paginator.loadMore()
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
            simpleProfileProvider: ISimpleProfileProvider,
            nozzleSubscriber: INozzleSubscriber,
            contactDao: ContactDao,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileListViewModel(
                        simpleProfileProvider = simpleProfileProvider,
                        nozzleSubscriber = nozzleSubscriber,
                        contactDao = contactDao
                    ) as T
                }
            }
    }
}
