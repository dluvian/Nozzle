package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.compose.runtime.Immutable
import com.dluvian.nozzle.data.room.helper.Nip65Relay

@Immutable
data class RelayEditorViewModelState(
    val myRelays: List<Nip65Relay> = emptyList(),
    val popularRelays: List<String> = emptyList(),
    val hasChanges: Boolean = false,
    val isError: Boolean = false,
    val isLoading: Boolean = false,
    val addIsEnabled: Boolean = true,
)
