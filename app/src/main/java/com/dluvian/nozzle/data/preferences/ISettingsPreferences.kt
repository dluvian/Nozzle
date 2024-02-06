package com.dluvian.nozzle.data.preferences

interface ISettingsPreferences : ISettingsPreferenceStates {

    fun showProfilePictures(): Boolean

    fun setShowProfilePictures(bool: Boolean)
}
