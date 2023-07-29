package com.dluvian.nozzle.ui.app.views.feed

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.nostr.INostrSubscriber
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
private const val DB_BATCH_SIZE = 30
private const val WAIT_TIME = 1300L

data class FeedViewModelState(
    val isRefreshing: Boolean = false,
    val pubkey: String = "",
    val feedSettings: FeedSettings = FeedSettings(
        isPosts = true,
        isReplies = true,
        authorSelection = Contacts,
        relaySelection = UserSpecific(emptyMap()),
    ),
    val relayStatuses: List<RelayActive> = emptyList(),
)

class FeedViewModel(
    private val personalProfileProvider: IPersonalProfileProvider,
    private val feedProvider: IFeedProvider,
    private val relayProvider: IRelayProvider,
    private val contactListProvider: IContactListProvider,
    private val postCardInteractor: IPostCardInteractor,
    private val nostrSubscriber: INostrSubscriber,
    private val feedSettingsPreferences: IFeedSettingsPreferences,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(FeedViewModelState())
    val uiState = viewModelState.stateIn(
        viewModelScope, SharingStarted.Eagerly, viewModelState.value
    )

    var metadataState = personalProfileProvider.getMetadata()
        .stateIn(
            viewModelScope, SharingStarted.Lazily, null
        )

    var feedState: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    private val lastAutopilotResult: MutableMap<String, Set<String>> =
        Collections.synchronizedMap(mutableMapOf<String, Set<String>>())

    init {
        Log.i(TAG, "Initialize FeedViewModel")
        viewModelState.update {
            it.copy(
                pubkey = personalProfileProvider.getPubkey(),
                feedSettings = feedSettingsPreferences.getFeedSettings(),
            )
        }
        viewModelScope.launch(context = IO) {
            handleRefresh()
        }
    }

    val onRefreshFeedView: () -> Unit = {
        viewModelScope.launch(context = IO) {
            Log.i(TAG, "Refresh feed view")
            handleRefresh()
        }
    }

    val onLoadMore: () -> Unit = {
        viewModelScope.launch(context = IO) {
            Log.i(TAG, "Load more")
            appendFeed(currentFeed = feedState.value)
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
                feedSettingsPreferences.setFeedSettings(viewModelState.value.feedSettings)
            }
        }
        toggledContacts = false
        toggledPosts = false
        toggledReplies = false
        toggledAutopilot = false
        toggledRelay = false
    }

    val onToggleContactsOnly: () -> Unit = {
        viewModelState.value.feedSettings.authorSelection.let { oldValue ->
            viewModelState.update {
                this.toggledContacts = !this.toggledContacts
                val newValue = when (oldValue) {
                    is Everyone -> Contacts
                    is Contacts -> Everyone
                    is SingleAuthor -> {
                        Log.w(
                            TAG,
                            "ContactsOnly is set to SingleAuthor, which shouldn't be possible"
                        )
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
        viewModelState.value.feedSettings.let { oldSettings ->
            // Only changeable when isReplies is active
            if (oldSettings.isReplies) {
                viewModelState.update {
                    this.toggledPosts = !this.toggledPosts
                    it.copy(feedSettings = it.feedSettings.copy(isPosts = !oldSettings.isPosts))
                }
            }
        }
    }

    val onToggleReplies: () -> Unit = {
        viewModelState.value.feedSettings.let { oldSettings ->
            // Only changeable when isPosts is active
            if (oldSettings.isPosts) {
                viewModelState.update {
                    this.toggledReplies = !this.toggledReplies
                    it.copy(feedSettings = it.feedSettings.copy(isReplies = !oldSettings.isReplies))
                }
            }
        }
    }

    val onToggleAutopilot: () -> Unit = {
        if (autopilotIsToggleable()) {
            this.toggledAutopilot = !this.toggledAutopilot
            viewModelState.value.feedSettings.relaySelection.let { oldValue ->
                // No need to set input. It will be updated in onRefreshOnMenuDismiss
                val newValue = if (oldValue is UserSpecific) {
                    MultipleRelays(relays = emptyList())
                } else UserSpecific(pubkeysPerRelay = lastAutopilotResult)
                viewModelState.update {
                    it.copy(feedSettings = it.feedSettings.copy(relaySelection = newValue))
                }
            }
        }
    }

    private fun autopilotIsToggleable(): Boolean {
        return viewModelState.value.feedSettings.authorSelection !is Everyone
    }

    val onToggleRelayIndex: (Int) -> Unit = { index ->
        this.toggledRelay = true
        val toggled = toggleRelay(relays = viewModelState.value.relayStatuses, index = index)
        if (toggled.any { it.isActive }) {
            viewModelState.update {
                it.copy(relayStatuses = toggled)
            }
        }
    }

    // TODO: This should be handled with a flow, not manually
    val onResetProfileIconUiState: () -> Unit = {
        Log.i(TAG, "Reset profile icon")
        metadataState = personalProfileProvider.getMetadata()
            .stateIn(
                viewModelScope, SharingStarted.Lazily, null
            )
        viewModelState.update {
            it.copy(pubkey = personalProfileProvider.getPubkey())
        }
    }

    val onLike: (String) -> Unit = { id ->
        uiState.value.let { _ ->
            feedState.value.find { it.id == id }?.let {
                viewModelScope.launch(context = IO) {
                    postCardInteractor.like(
                        postId = id,
                        postPubkey = it.pubkey,
                        relays = relayProvider.getWriteRelays()
                    )
                }
            }
        }
    }

    val onQuote: (String) -> Unit = { id ->
        uiState.value.let { _ ->
            feedState.value.find { it.id == id }?.let {
                viewModelScope.launch(context = IO) {
                    TODO()
                }
            }
        }
    }

    private suspend fun handleRefresh() {
        setUIRefresh(true)
        subscribeToNip65()
        // TODO: Update screen with latest. Freeze post ids once loading indicator is gone
        delay(WAIT_TIME)
        subscribeToPersonalProfile()
        updateScreen()
        setUIRefresh(false)
    }

    private suspend fun updateScreen() {
        updateRelaySelection()
        // TODO: combine flow instead of new stateIn??
        feedState = feedProvider.getFeedFlow(
            feedSettings = viewModelState.value.feedSettings,
            limit = DB_BATCH_SIZE,
            waitForSubscription = WAIT_TIME,
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            feedState.value,
        )
        delay(WAIT_TIME)
        renewAdditionalDataSubscription()
    }

    private val isAppending = AtomicBoolean(false)

    private suspend fun appendFeed(currentFeed: List<PostWithMeta>) {
        if (isAppending.get()) return

        currentFeed.lastOrNull()?.let { last ->
            Log.i(TAG, "Append feed")
            isAppending.set(true)
            feedState = feedProvider.getFeedFlow(
                feedSettings = viewModelState.value.feedSettings,
                limit = DB_BATCH_SIZE,
                until = last.createdAt,
            ).map { toAppend -> currentFeed + toAppend }
                .stateIn(
                    viewModelScope,
                    SharingStarted.Eagerly,
                    currentFeed,
                )
            Log.i(TAG, "New feed length ${feedState.value.size}")
            isAppending.set(false)
        }
        delay(WAIT_TIME)
        renewAdditionalDataSubscription()
    }

    private fun subscribeToPersonalProfile() {
        nostrSubscriber.unsubscribeProfiles()
        nostrSubscriber.subscribeToProfileMetadataAndContactList(
            pubkeys = listOf(personalProfileProvider.getPubkey()),
            relays = relayProvider.getWriteRelays(),
        )
    }

    private fun subscribeToNip65() {
        nostrSubscriber.unsubscribeNip65()
        nostrSubscriber.subscribeNip65(
            pubkeys = contactListProvider.listPersonalContactPubkeys() + personalProfileProvider.getPubkey()
        )
    }

    private suspend fun renewAdditionalDataSubscription() {
        nostrSubscriber.unsubscribeAdditionalPostsData()
        nostrSubscriber.subscribeToAdditionalPostsData(
            posts = feedState.value.takeLast(DB_BATCH_SIZE),
            relays = getSelectedRelays()
        )
    }

    private fun setUIRefresh(value: Boolean) {
        viewModelState.update { it.copy(isRefreshing = value) }
    }

    private suspend fun updateRelaySelection(newRelayStatuses: List<RelayActive>? = null) {
        val relaySelection = when (viewModelState.value.feedSettings.relaySelection) {
            is UserSpecific -> {
                val autopilotRelays = getAndCacheAutopilotRelays()
                if (autopilotRelays.any { it.value.isNotEmpty() }) {
                    UserSpecific(pubkeysPerRelay = autopilotRelays)
                } else {
                    AllRelays
                }
            }

            is MultipleRelays -> {
                val selectedRelays = (newRelayStatuses ?: viewModelState.value.relayStatuses)
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
        viewModelState.update {
            it.copy(
                relayStatuses = newStatuses,
                feedSettings = it.feedSettings.copy(relaySelection = relaySelection)
            )
        }
    }

    private fun getSelectedRelays(): Collection<String>? {
        return viewModelState.value.feedSettings.relaySelection.getSelectedRelays()
    }

    private suspend fun getAndCacheAutopilotRelays(): Map<String, Set<String>> {
        val result = relayProvider.getAutopilotRelays()
        lastAutopilotResult.clear()
        lastAutopilotResult.putAll(result)

        return result
    }

    companion object {
        fun provideFactory(
            personalProfileProvider: IPersonalProfileProvider,
            feedProvider: IFeedProvider,
            relayProvider: IRelayProvider,
            contactListProvider: IContactListProvider,
            postCardInteractor: IPostCardInteractor,
            nostrSubscriber: INostrSubscriber,
            feedSettingsPreferences: IFeedSettingsPreferences,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FeedViewModel(
                    personalProfileProvider = personalProfileProvider,
                    feedProvider = feedProvider,
                    relayProvider = relayProvider,
                    contactListProvider = contactListProvider,
                    postCardInteractor = postCardInteractor,
                    nostrSubscriber = nostrSubscriber,
                    feedSettingsPreferences = feedSettingsPreferences,
                ) as T
            }
        }
    }
}
