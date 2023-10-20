package com.dluvian.nozzle.ui.app.views.editProfile

data class EditProfileViewModelState(
    val nameInput: String = "",
    val aboutInput: String = "",
    val pictureInput: String = "",
    val nip05Input: String = "",
    val lud16Input: String = "",
    val hasChanges: Boolean = false,
    val isInvalidPictureUrl: Boolean = false,
)
