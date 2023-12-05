package com.dluvian.nozzle.ui.app.views.reply

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.postPreparer.IPostPreparer
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.HashtagDao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.entity.HashtagEntity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.utils.addLimitedRelayStatuses
import com.dluvian.nozzle.data.utils.listRelayStatuses
import com.dluvian.nozzle.data.utils.toggleRelay
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.ReplyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ReplyViewModel(
    private val nostrService: INostrService,
    private val pubkeyProvider: IPubkeyProvider,
    private val relayProvider: IRelayProvider,
    private val postPreparer: IPostPreparer,
    private val postDao: PostDao,
    private val hashtagDao: HashtagDao,
) : ViewModel() {
    private var recipientPubkey: String = ""
    private var postToReplyTo: PostWithMeta? = null

    val pubkeyState = pubkeyProvider.getActivePubkeyStateFlow()

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
                val relays = listRelayStatuses(
                    allRelayUrls = relayProvider.getWriteRelays(),
                    relaySelection = AllRelays
                )
                it.copy(
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

    private var debounceJob: Job? = null
    val onSearch: (String) -> Unit = { name ->
        debounceJob?.cancel()
        debounceJob = viewModelScope.launch(Dispatchers.IO) {
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
        val event = sendReply(parentPost = parentPost, state = uiState.value, input = input)
        viewModelScope.launch(context = Dispatchers.IO) {
            postDao.insertOrIgnore(PostEntity.fromEvent(event))
            // TODO: Insert hashtags in tx
            // TODO: dbSweepExcludingCache.addPostId(event.id)
            val hashtags = event.getHashtags()
                .map { HashtagEntity(eventId = event.id, hashtag = it.lowercase()) }
            if (hashtags.isNotEmpty()) {
                hashtagDao.insertOrIgnore(*hashtags.toTypedArray())
            }
        }
        resetUI()
    }

    private fun sendReply(
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
            .map { it.relayUrl }
        val post = postPreparer.getCleanPostWithTagsAndMentions(input)
        return nostrService.sendReply(
            replyTo = replyTo,
            content = post.content,
            mentions = (post.mentions + parentPost.pubkey).distinct(),
            hashtags = post.hashtags,
            relays = selectedRelays // TODO: Add read relays of mentioned pubkeys
        )
    }

    private fun resetUI() {
        _uiState.update {
            recipientPubkey = ""
            it.copy(
                recipientName = "",
                relaySelection = listRelayStatuses(
                    allRelayUrls = relayProvider.getWriteRelays(),
                    relaySelection = AllRelays,
                ),
            )
        }
    }

    companion object {
        fun provideFactory(
            nostrService: INostrService,
            pubkeyProvider: IPubkeyProvider,
            relayProvider: IRelayProvider,
            postPreparer: IPostPreparer,
            postDao: PostDao,
            hashtagDao: HashtagDao,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReplyViewModel(
                    nostrService = nostrService,
                    pubkeyProvider = pubkeyProvider,
                    relayProvider = relayProvider,
                    postPreparer = postPreparer,
                    postDao = postDao,
                    hashtagDao = hashtagDao,
                ) as T
            }
        }
    }
}
