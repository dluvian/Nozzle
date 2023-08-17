package com.dluvian.nozzle.ui.app.views.editProfile

import android.content.Context
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.manager.IPersonalProfileManager
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.utils.NostrUtils.isValidUsername
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "EditProfileViewModel"

data class EditProfileViewModelState(
    val nameInput: String = "",
    val aboutInput: String = "",
    val pictureInput: String = "",
    val nip05Input: String = "",
    val lud16Input: String = "",
    val hasChanges: Boolean = false,
    val isInvalidUsername: Boolean = false,
    val isInvalidPictureUrl: Boolean = false,
)

class EditProfileViewModel(
    private val personalProfileManager: IPersonalProfileManager,
    private val nostrService: INostrService,
    context: Context,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(EditProfileViewModelState())

    var metadataState = personalProfileManager.getMetadataStateFlow()

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value
        )

    init {
        Log.i(TAG, "Initialize EditProfileViewModel")
        useCachedValues()
    }

    val onUpdateProfile: () -> Unit = {
        uiState.value.let {
            if (!it.hasChanges) {
                Log.i(TAG, "Profile editor has no changes")
                return@let
            }
            val isValidUsername = isValidUsername(it.nameInput)
            val isValidUrl = isValidUrl(it.pictureInput)
            if (isValidUsername && isValidUrl) {
                Log.i(TAG, "New values are valid. Update profile")
                viewModelScope.launch(context = Dispatchers.IO) {
                    updateMetadataInDb(it)
                    updateMetadataOverNostr(it)
                    useCachedValues()
                }
                Toast.makeText(
                    context,
                    context.getString(R.string.profile_updated),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Log.i(TAG, "New values are invalid")
                viewModelState.update { state ->
                    state.copy(
                        isInvalidUsername = !isValidUsername,
                        isInvalidPictureUrl = !isValidUrl
                    )
                }
            }
        }
    }

    val onChangeName: (String) -> Unit = { input ->
        uiState.value.let { state ->
            viewModelState.update {
                it.copy(nameInput = input)
            }
            setUIHasChanges()
            if (state.isInvalidUsername && isValidUsername(input)) {
                viewModelState.update {
                    it.copy(isInvalidUsername = false)
                }
            }
        }
    }

    val onChangeAbout: (String) -> Unit = { input ->
        if (input != uiState.value.aboutInput) {
            viewModelState.update {
                it.copy(aboutInput = input)
            }
            setUIHasChanges()
        }
    }

    val onChangePicture: (String) -> Unit = { input ->
        uiState.value.let { state ->
            viewModelState.update {
                it.copy(pictureInput = input)
            }
            setUIHasChanges()
            if (state.isInvalidPictureUrl && isValidUrl(input)) {
                viewModelState.update {
                    it.copy(isInvalidPictureUrl = false)
                }
            }
        }
    }

    val onChangeNip05: (String) -> Unit = { input ->
        viewModelState.update {
            it.copy(nip05Input = input)
        }
        setUIHasChanges()
    }

    val onChangeLud16: (String) -> Unit = { input ->
        viewModelState.update {
            it.copy(lud16Input = input)
        }
        setUIHasChanges()
    }

    val onCanGoBack: () -> Boolean = {
        val canGoBack = uiState.value.let { state ->
            !state.isInvalidUsername && !state.isInvalidPictureUrl
        }
        canGoBack
    }

    val onResetUiState: () -> Unit = {
        Log.i(TAG, "Reset UI")
        useCachedValues()
    }

    private suspend fun updateMetadataInDb(state: EditProfileViewModelState) {
        Log.i(TAG, "Update profile in DB")
        personalProfileManager.setMeta(
            name = state.nameInput,
            about = state.aboutInput,
            picture = state.pictureInput,
            nip05 = state.nip05Input,
            lud16 = state.lud16Input,
        )
    }

    private fun updateMetadataOverNostr(state: EditProfileViewModelState) {
        Log.i(TAG, "Update profile over nostr")
        val metadata = Metadata(
            name = state.nameInput,
            about = state.aboutInput,
            picture = state.pictureInput,
            nip05 = state.nip05Input,
            lud16 = state.lud16Input,
        )
        nostrService.publishProfile(metadata = metadata)
    }

    private fun isValidUrl(url: String) = url.isEmpty() || URLUtil.isValidUrl(url)

    private fun setUIHasChanges() {
        metadataState.value.let { metadata ->
            uiState.value.let { oldState ->
                val hasChanges = oldState.nameInput != metadata?.name.orEmpty()
                        || oldState.aboutInput != metadata?.about.orEmpty()
                        || oldState.pictureInput != metadata?.picture.orEmpty()
                        || oldState.nip05Input != metadata?.nip05.orEmpty()
                        || oldState.lud16Input != metadata?.lud16.orEmpty()
                if (hasChanges != oldState.hasChanges) {
                    viewModelState.update { state ->
                        state.copy(hasChanges = hasChanges)
                    }
                }
            }
        }
    }

    private fun useCachedValues() {
        Log.i(TAG, "Use cached values")
        metadataState = personalProfileManager.getMetadataStateFlow()
        metadataState.value.let { metadata ->
            viewModelState.update {
                it.copy(
                    nameInput = metadata?.name.orEmpty(),
                    aboutInput = metadata?.about.orEmpty(),
                    pictureInput = metadata?.picture.orEmpty(),
                    nip05Input = metadata?.nip05.orEmpty(),
                    lud16Input = metadata?.lud16.orEmpty(),
                    hasChanges = false,
                    isInvalidUsername = false,
                    isInvalidPictureUrl = false
                )
            }
        }
    }


    companion object {
        fun provideFactory(
            personalProfileManager: IPersonalProfileManager,
            nostrService: INostrService,
            context: Context,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditProfileViewModel(
                    personalProfileManager = personalProfileManager,
                    nostrService = nostrService,
                    context = context
                ) as T
            }
        }
    }
}
