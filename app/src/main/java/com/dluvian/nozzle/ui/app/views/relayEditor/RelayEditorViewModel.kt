package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.UrlUtils.isWebsocketUrl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class RelayEditorViewModelState(
    val relays: List<Nip65Relay> = listOf(),
    val hasChanges: Boolean = false,
)

class RelayEditorViewModel(
    nostrService: INostrService,
    relayProvider: IRelayProvider
) : ViewModel() {

    private val uiFlow = MutableStateFlow(RelayEditorViewModelState())
    val uiState = uiFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        uiFlow.value
    )

    private val originalRelays: MutableList<Nip65Relay> = mutableListOf()
    val onOpenRelayEditor: () -> Unit = {
        clearAndAddRelays(list = originalRelays, toAdd = relayProvider.getNip65Relays())
        uiFlow.update {
            RelayEditorViewModelState(
                relays = originalRelays,
                hasChanges = false
            )
        }
    }

    val onSaveRelays: () -> Unit = {
        val relays = uiState.value.relays
        nostrService.publishNip65(nip65Relays = relays)
        // TODO: Save in db
        uiFlow.update { it.copy(relays = relays, hasChanges = false) }
        clearAndAddRelays(list = originalRelays, toAdd = relays)
    }

    val onAddRelay: (String) -> Boolean = local@{ url ->
        if (!url.isWebsocketUrl()) return@local false

        val relays = uiFlow.value.relays + Nip65Relay(url = url, isRead = true, isWrite = true)
        uiFlow.update {
            it.copy(
                relays = relays,
                hasChanges = relays != originalRelays
            )
        }
        return@local true
    }

    val onDeleteRelay: (Int) -> Unit = { index ->
        uiFlow.update {
            val relays = uiFlow.value.relays.filterIndexed { i, _ -> i != index }
            it.copy(
                relays = relays,
                hasChanges = relays != originalRelays
            )
        }
    }

    val onToggleWrite: (Int) -> Unit = { index ->
        uiFlow.update {
            val relays = it.relays.mapIndexed { i, nip65 ->
                if (i == index) nip65.copy(isWrite = !nip65.isWrite) else nip65
            }
            it.copy(
                relays = relays,
                hasChanges = relays != originalRelays
            )
        }
    }

    val onToggleRead: (Int) -> Unit = { index ->
        uiFlow.update {
            val relays = it.relays.mapIndexed { i, nip65 ->
                if (i == index) nip65.copy(isRead = !nip65.isRead) else nip65
            }
            it.copy(
                relays = relays,
                hasChanges = relays != originalRelays
            )
        }
    }

    private fun clearAndAddRelays(list: MutableList<Nip65Relay>, toAdd: List<Nip65Relay>) {
        synchronized(list) {
            list.clear()
            list.addAll(toAdd)
        }
    }

    companion object {
        fun provideFactory(
            nostrService: INostrService,
            relayProvider: IRelayProvider
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RelayEditorViewModel(
                        nostrService = nostrService,
                        relayProvider = relayProvider
                    ) as T
                }
            }
    }
}
