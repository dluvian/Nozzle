package com.dluvian.nozzle.ui.app.views.profileList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.KClass

class ProfileListViewModel : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    val forcedFollowState: StateFlow<MutableMap<Pubkey, Boolean>> = MutableStateFlow(mutableMapOf())
    var profileList: StateFlow<ProfileList?> = MutableStateFlow(null)

    private val isSettingPubkey = AtomicBoolean(false)
    val onSetProfileList: (Pubkey, KClass<ProfileList>) -> Unit = { pubkey, type ->
        TODO()
    }

    val onRefresh: () -> Unit = {
        TODO()
    }

    private var followProcesses: MutableMap<Pubkey, Job?> = mutableMapOf()
    val onFollow: (Int) -> Unit = { index ->
        TODO()
    }

    val onUnfollow: (Int) -> Unit = { index ->
        TODO()
    }

    companion object {
        fun provideFactory(
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileListViewModel(
                    ) as T
                }
            }
    }
}
