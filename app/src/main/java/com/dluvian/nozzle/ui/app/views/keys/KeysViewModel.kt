package com.dluvian.nozzle.ui.app.views.keys

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.manager.IPersonalProfileManager
import com.dluvian.nozzle.data.nostr.utils.KeyUtils.isValidPrivkey
import com.dluvian.nozzle.data.subscriber.INozzleSubscriber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

private const val TAG = "KeysViewModel"

class KeysViewModel(
    private val keyManager: IKeyManager,
    private val personalProfileManager: IPersonalProfileManager,
    private val nozzleSubscriber: INozzleSubscriber,
    context: Context,
    clip: ClipboardManager,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(KeysViewModelState())
    private var nsec = ""

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value
        )

    init {
        useCachedValues()
    }

    val onCopyNpub: () -> Unit = {
        uiState.value.npub.let {
            Log.i(TAG, "Copy npub $it")
            clip.setText(AnnotatedString(it))
            Toast.makeText(
                context,
                context.getString(R.string.profile_id_copied),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    val onUpdateKeyPair: (FocusManager) -> Unit = { focusManager ->
        uiState.value.let { state ->
            val isValid = isValidPrivkey(state.privkeyInput)
            if (!isValid) {
                Log.i(TAG, "New privkey ${state.privkeyInput} is invalid")
                viewModelState.update {
                    state.copy(isInvalid = true)
                }
            } else if (uiState.value.hasChanges) {
                Log.i(TAG, "Saving new privkey")
                keyManager.setPrivkey(state.privkeyInput)
                personalProfileManager.updateMetadata()
                nozzleSubscriber.subscribePersonalProfile()
                useCachedValues()
                focusManager.clearFocus()
                Toast.makeText(
                    context,
                    context.getString(R.string.key_pair_updated),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val onChangePrivkey: (String) -> Unit = { newValue ->
        if (nsec == newValue) {
            viewModelState.update {
                it.copy(
                    privkeyInput = newValue,
                    isInvalid = false,
                    hasChanges = false,
                )
            }
        } else if (uiState.value.privkeyInput != newValue) {
            viewModelState.update {
                it.copy(
                    privkeyInput = newValue,
                    isInvalid = false,
                    hasChanges = true,
                )
            }
        }
    }

    val onResetUiState: () -> Unit = {
        useCachedValues()
    }

    private fun useCachedValues() {
        nsec = keyManager.getNsec()
        viewModelState.update {
            it.copy(
                privkeyInput = nsec,
                npub = keyManager.getNpub(),
                hasChanges = false,
                isInvalid = false,
            )
        }
    }

    companion object {
        fun provideFactory(
            keyManager: IKeyManager,
            personalProfileManager: IPersonalProfileManager,
            nozzleSubscriber: INozzleSubscriber,
            context: Context,
            clip: ClipboardManager,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return KeysViewModel(
                        keyManager = keyManager,
                        personalProfileManager = personalProfileManager,
                        nozzleSubscriber = nozzleSubscriber,
                        context = context,
                        clip = clip
                    ) as T
                }
            }
    }
}
