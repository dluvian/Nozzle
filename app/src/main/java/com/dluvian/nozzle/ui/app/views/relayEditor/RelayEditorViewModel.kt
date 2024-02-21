package com.dluvian.nozzle.ui.app.views.relayEditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.data.SHORT_WAIT_TIME
import com.dluvian.nozzle.data.nip65Updater.INip65Updater
import com.dluvian.nozzle.data.provider.IOnlineStatusProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.UrlUtils.WEBSOCKET_PREFIX
import com.dluvian.nozzle.data.utils.UrlUtils.isWebsocketUrl
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
import com.dluvian.nozzle.model.OnlineStatus
import com.dluvian.nozzle.model.Relay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RelayEditorViewModel(
    private val relayProvider: IRelayProvider,
    private val onlineStatusProvider: IOnlineStatusProvider,
    private val nip65Updater: INip65Updater
) : ViewModel() {

    private val _uiState = MutableStateFlow(RelayEditorViewModelState())
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(),
        _uiState.value
    )

    var onlineStatuses: StateFlow<Map<Relay, OnlineStatus>> = MutableStateFlow(emptyMap())

    private val originalRelays: MutableList<Nip65Relay> = mutableListOf()

    private var openingJob: Job? = null
    val onOpenRelayEditor: () -> Unit = local@{
        if (openingJob?.isActive == true) return@local

        _uiState.update { it.copy(isLoading = true) }
        openingJob = viewModelScope.launch(context = Dispatchers.IO) {
            val relays = relayProvider.getNip65Relays()
            clearAndAddRelays(list = originalRelays, toAdd = relays)
            val popularRelays = relayProvider.getPopularRelays()
            setOnlineStatuses(relays = relays.map { it.url } + popularRelays)
            _uiState.update {
                RelayEditorViewModelState(
                    myRelays = relays,
                    popularRelays = popularRelays,
                    isError = false,
                    isLoading = false,
                    addIsEnabled = addRelayIsEnabled(relays.size)
                )
            }
        }
        openingJob?.invokeOnCompletion { _ ->
            _uiState.update { it.copy(isLoading = false) }
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
        setOnlineStatuses(relays = onlineStatuses.value.keys + url)
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

    private fun setOnlineStatuses(relays: Collection<Relay>) {
        onlineStatuses = onlineStatusProvider.getOnlineStatuses(relays = relays)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                onlineStatuses.value,
            )
    }

    private suspend fun publishAndSaveInDb(nip65Relays: List<Nip65Relay>) {
        _uiState.update {
            it.copy(
                myRelays = nip65Relays,
                isError = false,
                addIsEnabled = addRelayIsEnabled(nip65Relays.size)
            )
        }
        clearAndAddRelays(list = originalRelays, toAdd = nip65Relays)
        nip65Updater.publishAndSaveInDb(nip65Relays = nip65Relays)
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
            relayProvider: IRelayProvider,
            onlineStatusProvider: IOnlineStatusProvider,
            nip65Updater: INip65Updater,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return RelayEditorViewModel(
                        relayProvider = relayProvider,
                        onlineStatusProvider = onlineStatusProvider,
                        nip65Updater = nip65Updater
                    ) as T
                }
            }
    }
}
