package com.dluvian.nozzle.ui.app.views.feed

import android.util.Log
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
import com.dluvian.nozzle.data.utils.toggleRelay
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.Contacts
import com.dluvian.nozzle.model.CreatedAt
import com.dluvian.nozzle.model.Everyone
import com.dluvian.nozzle.model.MultipleRelays
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.SingleAuthor
import com.dluvian.nozzle.model.UserSpecific
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Collections

private const val TAG = "FeedViewModel"

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

    val pubkeyState = pubkeyProvider.getActivePubkeyStateFlow()
        .onEach local@{
            if (it.isEmpty()) return@local
            useCachedFeedSettings()
            updateRelaySelection()
            paginator.refresh(waitForSubscription = false, useInitialValue = false)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    init {
        useCachedFeedSettings()
    }

    val onRefresh: () -> Unit = {
        // Paginator can't set isRefreshing in time
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch(Dispatchers.IO) {
            updateRelaySelection()
            paginator.refresh(waitForSubscription = true, useInitialValue = true)
        }
    }

    val onLoadMore: () -> Unit = { paginator.loadMore() }

    private var toggledContacts = false
    private var toggledPosts = false
    private var toggledReplies = false
    private var toggledAutopilot = false
    private var toggledRelay = false

    val onRefreshOnMenuDismiss: () -> Unit = {
        if (toggledContacts || toggledPosts || toggledReplies || toggledAutopilot || toggledRelay) {
            onRefresh()
            if (toggledContacts || toggledPosts || toggledReplies) {
                feedSettingsPreferences.setFeedSettings(_uiState.value.feedSettings)
            }
        }
        toggledContacts = false
        toggledPosts = false
        toggledReplies = false
        toggledAutopilot = false
        toggledRelay = false
    }

    val onToggleContactsOnly: () -> Unit = {
        _uiState.value.feedSettings.authorSelection.let { oldValue ->
            _uiState.update {
                this.toggledContacts = !this.toggledContacts
                val newValue = when (oldValue) {
                    is Everyone -> Contacts
                    is Contacts -> Everyone
                    is SingleAuthor -> {
                        Log.w(TAG, "ContactsOnly is set to SingleAuthor")
                        Contacts
                    }
                }
                it.copy(
                    feedSettings = it.feedSettings.copy(
                        authorSelection = newValue,
                        // Autopilot is not allowed for global feed.
                        // Relays are set in onRefreshOnMenuDismiss.
                        relaySelection = if (newValue is Everyone) MultipleRelays(emptyList())
                        else it.feedSettings.relaySelection
                    )
                )
            }
        }
    }

    val onTogglePosts: () -> Unit = {
        _uiState.value.feedSettings.let { oldSettings ->
            // Only changeable when isReplies is active
            if (oldSettings.isReplies) {
                _uiState.update {
                    this.toggledPosts = !this.toggledPosts
                    it.copy(feedSettings = it.feedSettings.copy(isPosts = !oldSettings.isPosts))
                }
            }
        }
    }

    val onToggleReplies: () -> Unit = {
        _uiState.value.feedSettings.let { oldSettings ->
            // Only changeable when isPosts is active
            if (oldSettings.isPosts) {
                _uiState.update {
                    this.toggledReplies = !this.toggledReplies
                    it.copy(feedSettings = it.feedSettings.copy(isReplies = !oldSettings.isReplies))
                }
            }
        }
    }

    val onToggleAutopilot: () -> Unit = {
        if (autopilotIsToggleable()) {
            this.toggledAutopilot = !this.toggledAutopilot
            _uiState.value.feedSettings.relaySelection.let { oldValue ->
                // No need to set input. It will be updated in onRefreshOnMenuDismiss
                val newValue = if (oldValue is UserSpecific) {
                    MultipleRelays(relays = emptyList())
                } else UserSpecific(pubkeysPerRelay = lastAutopilotResult)
                _uiState.update {
                    it.copy(feedSettings = it.feedSettings.copy(relaySelection = newValue))
                }
            }
        }
    }

    private fun useCachedFeedSettings() {
        _uiState.update {
            it.copy(feedSettings = feedSettingsPreferences.getFeedSettings())
        }
    }

    private fun autopilotIsToggleable(): Boolean {
        return _uiState.value.feedSettings.authorSelection !is Everyone
    }

    val onToggleRelayIndex: (Int) -> Unit = { index ->
        this.toggledRelay = true
        val toggled = toggleRelay(relays = _uiState.value.relayStatuses, index = index)
        if (toggled.any { it.isActive }) {
            _uiState.update {
                it.copy(relayStatuses = toggled)
            }
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
