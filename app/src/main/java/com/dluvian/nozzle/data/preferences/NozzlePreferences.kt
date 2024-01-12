package com.dluvian.nozzle.data.preferences

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf
import com.dluvian.nozzle.data.PreferenceFileNames

const val IS_DARK_MODE: String = "is_dark_mode"

class NozzlePreferences(
    private val context: Context
) : IDarkModePreferences {
    private val preferences = context.getSharedPreferences(
        PreferenceFileNames.NOZZLE,
        Context.MODE_PRIVATE
    )

    override val isDarkMode = mutableStateOf(isDarkMode())

    override fun setDarkMode(isDarkMode: Boolean) {
        this.isDarkMode.value = isDarkMode
        preferences.edit()
            .putBoolean(IS_DARK_MODE, isDarkMode)
            .apply()
    }

    private fun isDarkMode(): Boolean {
        val default = when (
            context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        ) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
        return preferences.getBoolean(IS_DARK_MODE, default)
    }
}
