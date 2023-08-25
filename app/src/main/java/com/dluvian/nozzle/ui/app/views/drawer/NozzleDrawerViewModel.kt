package com.dluvian.nozzle.ui.app.views.drawer


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.provider.IPersonalProfileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "NozzleDrawerViewModel"

// TODO: Flow from DB instead of manually setting state
data class DrawerViewModelState(
    val pubkey: String = "",
    val npub: String = "",
)

class NozzleDrawerViewModel(
    private val personalProfileProvider: IPersonalProfileProvider,
) : ViewModel() {
    private val drawerViewModelState = MutableStateFlow(DrawerViewModelState())

    var metadataState = personalProfileProvider.getMetadataStateFlow()

    val pubkeyState = drawerViewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            drawerViewModelState.value
        )

    init {
        Log.i(TAG, "Initialize NozzleDrawerViewModel")
        viewModelScope.launch(context = Dispatchers.IO) {
            useCachedValues()
        }
    }

    val onResetUiState: () -> Unit = {
        Log.i(TAG, "Reset UI")
        // TODO: USE FLOWS
        metadataState = personalProfileProvider.getMetadataStateFlow()
        useCachedValues()
    }

    private fun useCachedValues() {
        Log.i(TAG, "Set cached values")
        drawerViewModelState.update {
            it.copy(
                pubkey = personalProfileProvider.getPubkey(),
                npub = personalProfileProvider.getNpub(),
            )
        }
    }

    companion object {
        fun provideFactory(personalProfileProvider: IPersonalProfileProvider): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return NozzleDrawerViewModel(
                        personalProfileProvider = personalProfileProvider,
                    ) as T
                }
            }
    }
}
