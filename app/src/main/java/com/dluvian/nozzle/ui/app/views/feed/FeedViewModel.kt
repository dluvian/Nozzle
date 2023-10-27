package com.dluvian.nozzle.ui.app.views.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_APPEND_BATCH_SIZE
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.MAX_FEED_LENGTH
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.preferences.IFeedSettingsPreferences
import com.dluvian.nozzle.data.provider.*
import com.dluvian.nozzle.data.utils.*
import com.dluvian.nozzle.model.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "FeedViewModel"

class FeedViewModel(
    val clickedMediaUrlCache: IClickedMediaUrlCache,
    val postCardInteractor: IPostCardInteractor,
    private val personalProfileProvider: IPersonalProfileProvider,
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

    val metadataState = personalProfileProvider.getMetadataStateFlow()
    var feedState: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    private val lastAutopilotResult: MutableMap<String, Set<String>> =
        Collections.synchronizedMap(mutableMapOf<String, Set<String>>())

    private val isAppending = AtomicBoolean(false)

    val pubkeyState = pubkeyProvider.getActivePubkeyStateFlow()
        .onEach {
            useCachedFeedSettings()
            handleRefresh(delayBeforeUpdate = false)
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, pubkeyProvider.getActivePubkey())

    init {
        useCachedFeedSettings()
    }

    val onRefreshFeedView: () -> Unit = {
        viewModelScope.launch(context = IO) {
            handleRefresh(delayBeforeUpdate = true)
        }
    }

    val onLoadMore: () -> Unit = {
        if (!isAppending.get()) {
            isAppending.set(true)
            viewModelScope.launch(context = IO) {
                Log.i(TAG, "Load more")
                appendFeed(currentFeed = feedState.value)
                isAppending.set(false)
            }
        }
    }

    private var toggledContacts = false
    private var toggledPosts = false
    private var toggledReplies = false
    private var toggledAutopilot = false
    private var toggledRelay = false

    val onRefreshOnMenuDismiss: () -> Unit = {
        if (toggledContacts || toggledPosts || toggledReplies || toggledAutopilot || toggledRelay) {
            onRefreshFeedView()
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

    private suspend fun handleRefresh(delayBeforeUpdate: Boolean) {
        setUIRefresh(true)
        updateScreen()
        if (delayBeforeUpdate) delay(WAIT_TIME)
        setUIRefresh(false)
    }

    private suspend fun updateScreen() {
        updateRelaySelection()
        feedState = feedProvider.getFeedFlow(
            feedSettings = _uiState.value.feedSettings,
            limit = DB_BATCH_SIZE,
            waitForSubscription = WAIT_TIME,
        ).stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            feedState.value,
        )
    }

    // TODO: This is too slow and sucks. Same in ProfileViewModel. Use pagination
    private suspend fun appendFeed(currentFeed: List<PostWithMeta>) {
        setUIRefresh(true)
        currentFeed.lastOrNull()?.let { last ->
            feedState = feedProvider.getFeedFlow(
                feedSettings = _uiState.value.feedSettings,
                limit = DB_APPEND_BATCH_SIZE,
                until = last.entity.createdAt,
            ).distinctUntilChanged()
                .map { toAppend -> currentFeed.takeLast(MAX_FEED_LENGTH) + toAppend }
                .stateIn(
                    viewModelScope,
                    SharingStarted.Eagerly,
                    currentFeed,
                )
        }
        delay(WAIT_TIME)
        setUIRefresh(false)
    }

    private fun setUIRefresh(value: Boolean) {
        _uiState.update { it.copy(isRefreshing = value) }
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
            allRelayUrls = (lastAutopilotResult.map { it.key } + relayProvider.getReadRelays())
                .distinct(),
            relaySelection = relaySelection
        )
        _uiState.update {
            it.copy(
                relayStatuses = newStatuses,
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
            clickedMediaUrlCache: IClickedMediaUrlCache,
            postCardInteractor: IPostCardInteractor,
            personalProfileProvider: IPersonalProfileProvider,
            pubkeyProvider: IPubkeyProvider,
            feedProvider: IFeedProvider,
            relayProvider: IRelayProvider,
            autopilotProvider: IAutopilotProvider,
            feedSettingsPreferences: IFeedSettingsPreferences,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(
                    clickedMediaUrlCache = clickedMediaUrlCache,
                    postCardInteractor = postCardInteractor,
                    personalProfileProvider = personalProfileProvider,
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
