package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.data.SHORT_WAIT_TIME
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RelayEditorViewModel(
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
    private val pubkeyProvider: IPubkeyProvider,
    private val nip65Dao: Nip65Dao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RelayEditorViewModelState())
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        _uiState.value
    )

    private val originalRelays: MutableList<Nip65Relay> = mutableListOf()
    val onOpenRelayEditor: () -> Unit = {
        _uiState.update { it.copy(isLoading = true) }
        val relays = relayProvider.getNip65Relays()
        clearAndAddRelays(list = originalRelays, toAdd = relays)
        viewModelScope.launch(context = Dispatchers.IO) {
            _uiState.update {
                RelayEditorViewModelState(
                    myRelays = relays,
                    popularRelays = relayProvider.getRelaysOfContacts(),
                    isError = false,
                    isLoading = false,
                    addIsEnabled = addRelayIsEnabled(relays.size)
                )
            }
        }
    }

    val onSaveRelays: () -> Unit = {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch(context = Dispatchers.IO) {
            publishAndSaveInDb(nip65Relays = uiState.value.myRelays)
            delay(SHORT_WAIT_TIME)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    val onAddRelay: (String) -> Boolean = local@{ rawUrl ->
        val url = rawUrl.removeTrailingSlashes()
            .let { if (it.startsWith(WEBSOCKET_PREFIX)) it else "$WEBSOCKET_PREFIX$it" }
        if (url.isEmpty() || uiState.value.myRelays.any { it.url == url } || !url.isWebsocketUrl()) {
            if (url != WEBSOCKET_PREFIX) _uiState.update { it.copy(isError = true) }
            return@local false
        }

        val relays = _uiState.value.myRelays + Nip65Relay(url = url, isRead = true, isWrite = true)
        _uiState.update {
            it.copy(
                myRelays = relays,
                isError = false,
                addIsEnabled = addRelayIsEnabled(relays.size)
            )
        }
        return@local true
    }

    val onDeleteRelay: (Int) -> Unit = local@{ index ->
        if (uiState.value.myRelays.size <= 1) return@local

        _uiState.update {
            val relays = _uiState.value.myRelays.filterIndexed { i, _ -> i != index }
            it.copy(
                myRelays = relays,
                addIsEnabled = addRelayIsEnabled(relays.size)
            )
        }
    }

    val onToggleWrite: (Int) -> Unit = { index ->
        _uiState.update {
            val relays = it.myRelays.mapIndexed { i, nip65 ->
                if (i == index) nip65.copy(isWrite = !nip65.isWrite) else nip65
            }
            it.copy(myRelays = relays)
        }
    }

    val onToggleRead: (Int) -> Unit = { index ->
        _uiState.update {
            val relays = it.myRelays.mapIndexed { i, nip65 ->
                if (i == index) nip65.copy(isRead = !nip65.isRead) else nip65
            }
            it.copy(myRelays = relays)
        }
    }

    val onUsePopularRelay: (Int) -> Unit = local@{ index ->
        val relay = uiState.value.popularRelays[index]
        if (uiState.value.myRelays.any { it.url == relay }) {
            return@local
        }

        _uiState.update {
            val myRelays = it.myRelays + Nip65Relay(url = relay, isRead = true, isWrite = true)
            it.copy(
                myRelays = myRelays,
                popularRelays = it.popularRelays,
                addIsEnabled = addRelayIsEnabled(myRelays.size)
            )
        }
    }

    private suspend fun publishAndSaveInDb(nip65Relays: List<Nip65Relay>) {
        val event = nostrService.publishNip65(nip65Relays = nip65Relays)
        _uiState.update {
            it.copy(
                myRelays = nip65Relays,
                isError = false,
                addIsEnabled = addRelayIsEnabled(nip65Relays.size)
            )
        }
        clearAndAddRelays(list = originalRelays, toAdd = nip65Relays)
        val entities = nip65Relays.map {
            Nip65Entity(
                url = it.url,
                isRead = it.isRead,
                isWrite = it.isWrite,
                pubkey = pubkeyProvider.getActivePubkey(),
                createdAt = event.createdAt
            )
        }
        nip65Dao.insertAndDeleteOutdated(nip65s = entities)
    }

    private fun clearAndAddRelays(list: MutableList<Nip65Relay>, toAdd: List<Nip65Relay>) {
        synchronized(list) {
            list.clear()
            list.addAll(toAdd)
        }
    }

    private fun addRelayIsEnabled(relaySize: Int) = relaySize < MAX_RELAYS

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
