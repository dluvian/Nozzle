package com.dluvian.nozzle.ui.app.views.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.preferences.IFeedSettingsPreferences
import com.dluvian.nozzle.data.provider.IAutopilotProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.data.utils.listRelayStatuses
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.CreatedAt
import com.dluvian.nozzle.model.MultipleRelays
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.UserSpecific
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections

class FeedViewModel(
    private val pubkeyProvider: IPubkeyProvider,
    private val feedProvider: IFeedProvider,
    private val relayProvider: IRelayProvider,
    private val autopilotProvider: IAutopilotProvider,
    private val feedSettingsPreferences: IFeedSettingsPreferences,
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedViewModelState())
    val uiState = _uiState.stateIn(
        viewModelScope, SharingStarted.Eagerly, _uiState.value
    )

    private val paginator: IPaginator<PostWithMeta, CreatedAt> = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _uiState.update { it.copy(isRefreshing = bool) } },
        onGetPage = { lastCreatedAt, waitForSubscription ->
            feedProvider.getFeedFlow(
                feedSettings = _uiState.value.feedSettings,
                limit = DB_BATCH_SIZE,
                until = lastCreatedAt,
                waitForSubscription = waitForSubscription
            )
        },
        onIdentifyLastParam = { post -> post?.entity?.createdAt ?: getCurrentTimeInSeconds() }
    )

    val feed = paginator.getList()
    val numOfNewPosts = paginator.getNumOfNewItems()

    private val lastAutopilotResult: MutableMap<String, Set<String>> =
        Collections.synchronizedMap(mutableMapOf<String, Set<String>>())

    private var isInit = true
    val pubkeyState = pubkeyProvider.getActivePubkeyStateFlow()
        .onEach local@{
            if (it.isEmpty()) return@local
            useCachedFeedSettings()
            updateRelaySelection()
            paginator.refresh(waitForSubscription = isInit, useInitialValue = false)
            isInit = false
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    init {
        useCachedFeedSettings()
    }

    val onRefresh: () -> Unit = { refresh() }

    val onLoadMore: () -> Unit = { paginator.loadMore() }

    private fun refresh() {
        // Paginator can't set isRefreshing in time
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch(Dispatchers.IO) {
            updateRelaySelection()
            paginator.refresh(waitForSubscription = true, useInitialValue = true)
        }
    }

    private fun useCachedFeedSettings() {
        _uiState.update {
            it.copy(feedSettings = feedSettingsPreferences.getFeedSettings())
        }
    }

    private suspend fun updateRelaySelection(newRelayStatuses: List<RelayActive>? = null) {
        val relaySelection = when (_uiState.value.feedSettings.relaySelection) {
            is UserSpecific -> {
                val autopilotRelays = getAndCacheAutopilotRelays()
                if (autopilotRelays.any { it.value.isNotEmpty() }) {
                    UserSpecific(pubkeysPerRelay = autopilotRelays)
                } else {
                    AllRelays
                }
            }

            is MultipleRelays -> {
                val selectedRelays = (newRelayStatuses ?: _uiState.value.relayStatuses)
                    .filter { it.isActive }
                    .map { it.relayUrl }
                MultipleRelays(relays = selectedRelays)
            }

            is AllRelays -> AllRelays
        }
        val newStatuses = newRelayStatuses ?: listRelayStatuses(
            allRelayUrls = lastAutopilotResult.map { it.key } + relayProvider.getReadRelays(),
            relaySelection = relaySelection
        )
        _uiState.update {
            it.copy(
                relayStatuses = newStatuses.sortedByDescending { relay -> relay.count },
                feedSettings = it.feedSettings.copy(relaySelection = relaySelection)
            )
        }
    }

    private suspend fun getAndCacheAutopilotRelays(): Map<String, Set<String>> {
        val result = autopilotProvider.getAutopilotRelays()
        lastAutopilotResult.clear()
        lastAutopilotResult.putAll(result)

        return result
    }

    companion object {
        fun provideFactory(
            pubkeyProvider: IPubkeyProvider,
            feedProvider: IFeedProvider,
            relayProvider: IRelayProvider,
            autopilotProvider: IAutopilotProvider,
            feedSettingsPreferences: IFeedSettingsPreferences,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(
                    pubkeyProvider = pubkeyProvider,
                    feedProvider = feedProvider,
                    relayProvider = relayProvider,
                    autopilotProvider = autopilotProvider,
                    feedSettingsPreferences = feedSettingsPreferences,
                ) as T
            }
        }
    }
}
