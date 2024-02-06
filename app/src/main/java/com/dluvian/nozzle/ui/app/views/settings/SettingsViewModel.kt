package com.dluvian.nozzle.ui.app.views.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dluvian.nozzle.data.preferences.ISettingsPreferenceStates
import com.dluvian.nozzle.data.preferences.ISettingsPreferences

class SettingsViewModel(private val settingsPreferences: ISettingsPreferences) : ViewModel() {
    val settingsPreferenceStates: ISettingsPreferenceStates = settingsPreferences

    val onToggleShowProfilePictures: () -> Unit = {
        settingsPreferences.setShowProfilePictures(
            bool = !settingsPreferences.showProfilePictures.value
        )
    }

    companion object {
        fun provideFactory(
            settingsPreferences: ISettingsPreferences
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(
                        settingsPreferences = settingsPreferences
                    ) as T
                }
            }
    }
}
