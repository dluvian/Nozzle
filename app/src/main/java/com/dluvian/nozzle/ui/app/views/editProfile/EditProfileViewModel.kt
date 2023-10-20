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
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

private const val TAG = "EditProfileViewModel"

class EditProfileViewModel(
    private val personalProfileManager: IPersonalProfileManager,
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
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
        useCachedValues()
    }

    val onUpdateProfile: () -> Unit = {
        uiState.value.let {
            if (!it.hasChanges) {
                Log.i(TAG, "Profile editor has no changes")
                return@let
            }
            val isValidUrl = isValidUrl(it.pictureInput)
            if (isValidUrl) {
                Log.i(TAG, "New values are valid. Update profile")
                viewModelScope.launch(context = Dispatchers.IO) {
                    val newMetadata = updateMetadataOverNostr(it)
                    updateMetadataInDb(metadata = newMetadata)
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
                    state.copy(isInvalidPictureUrl = false)
                }
            }
        }
    }

    val onChangeName: (String) -> Unit = { input ->
        uiState.value.let {
            viewModelState.update { it.copy(nameInput = input) }
            setUIHasChanges()
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
        uiState.value.let { !it.isInvalidPictureUrl }
    }

    val onResetUiState: () -> Unit = {
        Log.i(TAG, "Reset UI")
        useCachedValues()
    }

    private suspend fun updateMetadataInDb(metadata: Metadata) {
        Log.i(TAG, "Update profile in DB")
        personalProfileManager.setMeta(
            name = metadata.name.orEmpty(),
            about = metadata.about.orEmpty(),
            picture = metadata.picture.orEmpty(),
            nip05 = metadata.nip05.orEmpty(),
            lud16 = metadata.lud16.orEmpty(),
        )
    }

    private fun updateMetadataOverNostr(state: EditProfileViewModelState): Metadata {
        Log.i(TAG, "Update profile over nostr")
        val metadata = Metadata(
            name = state.nameInput.trim(),
            about = state.aboutInput.trim(),
            picture = state.pictureInput.trim(),
            nip05 = state.nip05Input.trim(),
            lud16 = state.lud16Input.trim(),
        )
        nostrService.publishProfile(metadata = metadata, relays = relayProvider.getWriteRelays())
        return metadata
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
                    isInvalidPictureUrl = false
                )
            }
        }
    }


    companion object {
        fun provideFactory(
            personalProfileManager: IPersonalProfileManager,
            nostrService: INostrService,
            relayProvider: IRelayProvider,
            context: Context,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditProfileViewModel(
                    personalProfileManager = personalProfileManager,
                    nostrService = nostrService,
                    relayProvider = relayProvider,
                    context = context
                ) as T
            }
        }
    }
}
