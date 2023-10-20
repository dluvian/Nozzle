package com.dluvian.nozzle.ui.app.views.keys

data class KeysViewModelState(
    val npub: String = "",
    val privkeyInput: String = "",
    val hasChanges: Boolean = false,
    val isInvalid: Boolean = false,
)
