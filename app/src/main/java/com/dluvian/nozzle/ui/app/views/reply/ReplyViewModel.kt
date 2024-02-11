package com.dluvian.nozzle.ui.app.views.reply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.postPreparer.IPostPreparer
import com.dluvian.nozzle.data.provider.IPersonalProfileProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.FullPostInserter
import com.dluvian.nozzle.data.utils.addLimitedRelayStatuses
import com.dluvian.nozzle.data.utils.listRelaySelection
import com.dluvian.nozzle.data.utils.toggleRelay
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.ReplyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReplyViewModel(
    private val nostrService: INostrService,
    private val pubkeyProvider: IPubkeyProvider,
    private val personalProfileProvider: IPersonalProfileProvider,
    private val relayProvider: IRelayProvider,
    private val postPreparer: IPostPreparer,
    private val fullPostInserter: FullPostInserter,
    private val dbExcludingCache: IIdCache,
) : ViewModel() {
    private var recipientPubkey: String = ""
    private var postToReplyTo: PostWithMeta? = null

    val pubkeyState = pubkeyProvider.getActivePubkeyStateFlow()
    val pictureState = personalProfileProvider.getMetadataStateFlow()
        .map { it?.picture }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _uiState = MutableStateFlow(ReplyViewModelState())
    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            _uiState.value
        )

    val onPrepareReply: (PostWithMeta) -> Unit = { post ->
        // TODO: Use flows. This should not be needed
        postToReplyTo = post
        viewModelScope.launch(context = Dispatchers.IO) {
            _uiState.update {
                recipientPubkey = post.pubkey
                val relays = listRelaySelection(allRelays = relayProvider.getWriteRelays())
                it.copy(
                    searchSuggestions = emptyList(),
                    recipientName = post.name.ifEmpty {
                        getShortenedNpubFromPubkey(post.pubkey) ?: post.pubkey
                    },
                    relaySelection = addLimitedRelayStatuses(
                        list = relays,
                        relaysUrlsToAdd = relayProvider
                            .getReadRelaysOfPubkey(recipientPubkey)
                            .ifEmpty { post.relays }
                    )
                )
            }
        }
    }

    val onToggleRelaySelection: (Int) -> Unit = { index ->
        val toggled = toggleRelay(relays = uiState.value.relaySelection, index = index)
        if (toggled.any { it.isActive }) {
            _uiState.update { it.copy(relaySelection = toggled) }
        }
    }

    private var searchJob: Job? = null
    val onSearch: (String) -> Unit = { name ->
        searchJob?.cancel()
        searchJob = viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {
                it.copy(searchSuggestions = postPreparer.searchProfiles(nameLike = name))
            }
        }
    }

    val onClickMention: (Pubkey) -> Unit = { pubkey ->
        _uiState.update { it.copy(searchSuggestions = emptyList()) }
        viewModelScope.launch(Dispatchers.IO) {
            val relaySelection = addLimitedRelayStatuses(
                list = uiState.value.relaySelection,
                relaysUrlsToAdd = relayProvider.getReadRelaysOfPubkey(pubkey = pubkey)
            )
            _uiState.update { it.copy(relaySelection = relaySelection) }
        }
    }

    val onSend: (String) -> Unit = local@{ input ->
        val parentPost = postToReplyTo ?: return@local
        viewModelScope.launch(context = Dispatchers.IO) {
            val event = sendReply(parentPost = parentPost, state = uiState.value, input = input)
            fullPostInserter.insertFullPost(events = listOf(event))
            dbExcludingCache.addPostIds(ids = listOf(event.id))
        }
        resetUI()
    }

    private suspend fun sendReply(
        parentPost: PostWithMeta,
        state: ReplyViewModelState,
        input: String
    ): Event {
        val replyTo = ReplyTo(
            replyTo = parentPost.entity.id,
            relayUrl = parentPost.relays
                .filter { relayProvider.getWriteRelays().contains(it) }
                .randomOrNull()
                ?: parentPost.relays.randomOrNull(),
        )
        val selectedRelays = state.relaySelection
            .filter { it.isActive }
            .map { it.relay }
        val post = postPreparer.getCleanPostWithTagsAndMentions(input)
        return nostrService.sendReply(
            replyTo = replyTo,
            content = post.content,
            mentions = (post.mentions + parentPost.pubkey).distinct(),
            hashtags = post.hashtags,
            relays = selectedRelays
        )
    }

    private fun resetUI() {
        _uiState.update {
            recipientPubkey = ""
            it.copy(
                searchSuggestions = emptyList(),
                recipientName = "",
                relaySelection = listRelaySelection(allRelays = relayProvider.getWriteRelays()),
            )
        }
    }

    companion object {
        fun provideFactory(
            nostrService: INostrService,
            pubkeyProvider: IPubkeyProvider,
            personalProfileProvider: IPersonalProfileProvider,
            relayProvider: IRelayProvider,
            postPreparer: IPostPreparer,
            fullPostInserter: FullPostInserter,
            dbExcludingCache: IIdCache,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReplyViewModel(
                    nostrService = nostrService,
                    pubkeyProvider = pubkeyProvider,
                    personalProfileProvider = personalProfileProvider,
                    relayProvider = relayProvider,
                    postPreparer = postPreparer,
                    fullPostInserter = fullPostInserter,
                    dbExcludingCache = dbExcludingCache
                ) as T
            }
        }
    }
}
