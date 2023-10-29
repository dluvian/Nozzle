package com.dluvian.nozzle.ui.app.views.editProfile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.manager.IPersonalProfileManager
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val personalProfileManager: IPersonalProfileManager,
) : ViewModel() {
    val metadataState = personalProfileManager.getMetadataStateFlow()

    val onUpsertProfile: (Metadata) -> Unit = { metadata ->
        viewModelScope.launch(context = Dispatchers.IO) {
            personalProfileManager.upsertMetadata(
                metadata = metadata
            )
        }
    }

    companion object {
        fun provideFactory(
            personalProfileManager: IPersonalProfileManager,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return EditProfileViewModel(
                    personalProfileManager = personalProfileManager,
                ) as T
            }
        }
    }
}
