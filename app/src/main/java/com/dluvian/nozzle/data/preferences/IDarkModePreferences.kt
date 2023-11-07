package com.dluvian.nozzle.data.preferences

import androidx.compose.runtime.State

interface IDarkModePreferences {
    val isDarkMode: State<Boolean>
    fun setDarkMode(isDarkMode: Boolean)
}
