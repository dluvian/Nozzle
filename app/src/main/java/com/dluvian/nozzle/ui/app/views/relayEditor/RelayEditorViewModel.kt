package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.UrlUtils.WEBSOCKET_PREFIX
import com.dluvian.nozzle.data.utils.UrlUtils.isWebsocketUrl
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RelayEditorViewModelState(
    val relays: List<Nip65Relay> = listOf(),
    val hasChanges: Boolean = false,
    val isError: Boolean = false,
)

class RelayEditorViewModel(
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val nip65Dao: Nip65Dao
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
                hasChanges = false,
                isError = false
            )
        }
    }

    val onSaveRelays: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            publishAndSaveInDb(nip65Relays = uiState.value.relays)
        }
    }

    val onAddRelay: (String) -> Boolean = local@{ rawUrl ->
        val url = rawUrl.removeTrailingSlashes()
            .let { if (it.startsWith(WEBSOCKET_PREFIX)) it else "$WEBSOCKET_PREFIX$it" }
        if (url.isEmpty() || uiFlow.value.relays.any { it.url == url } || !url.isWebsocketUrl()) {
            if (url != WEBSOCKET_PREFIX) uiFlow.update { it.copy(isError = true) }
            return@local false
        }

        val relays = uiFlow.value.relays + Nip65Relay(url = url, isRead = true, isWrite = true)
        uiFlow.update {
            it.copy(
                relays = relays,
                hasChanges = relays != originalRelays,
                isError = false
            )
        }
        return@local true
    }

    val onDeleteRelay: (Int) -> Unit = local@{ index ->
        if (uiState.value.relays.size <= 1) return@local

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

    private suspend fun publishAndSaveInDb(nip65Relays: List<Nip65Relay>) {
        val event = nostrService.publishNip65(nip65Relays = nip65Relays)
        uiFlow.update { it.copy(relays = nip65Relays, hasChanges = false) }
        clearAndAddRelays(list = originalRelays, toAdd = nip65Relays)
        val entities = nip65Relays.map {
            Nip65Entity(
                url = it.url,
                isRead = it.isRead,
                isWrite = it.isWrite,
                pubkey = pubkeyProvider.getPubkey(),
                createdAt = event.createdAt
            )
        }
        nip65Dao.insertAndDeleteOutdated(
            pubkey = pubkeyProvider.getPubkey(),
            timestamp = event.createdAt,
            nip65Entities = entities.toTypedArray()
        )
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
            relayProvider: IRelayProvider,
            pubkeyProvider: IPubkeyProvider,
            nip65Dao: Nip65Dao
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RelayEditorViewModel(
                        nostrService = nostrService,
                        relayProvider = relayProvider,
                        pubkeyProvider = pubkeyProvider,
                        nip65Dao = nip65Dao
                    ) as T
                }
            }
    }
}
