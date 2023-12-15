package com.dluvian.nozzle.ui.app.views.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.annotatedContent.IAnnotatedContentHandler
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.createNeventUri
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.postPreparer.IPostPreparer
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.FullPostInserter
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.utils.addLimitedRelayStatuses
import com.dluvian.nozzle.data.utils.listRelayStatuses
import com.dluvian.nozzle.data.utils.toggleRelay
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.nostr.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class PostViewModel(
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
    private val postPreparer: IPostPreparer,
    private val annotatedContentHandler: IAnnotatedContentHandler,
    private val fullPostInserter: FullPostInserter,
    private val dbExcludingCache: IIdCache,
    private val postDao: PostDao,
) : ViewModel() {
    val pubkeyState = pubkeyProvider.getActivePubkeyStateFlow()

    private val _uiState = MutableStateFlow(PostViewModelState())
    val uiState = _uiState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            _uiState.value
        )

    val onPreparePost: () -> Unit = {
        preparePost(postToQuote = null)
    }

    private val isPreparing = AtomicBoolean(false)
    val onPrepareQuote: (String) -> Unit = local@{ postIdToQuote ->
        val nostrId = nostrStrToNostrId(postIdToQuote) ?: return@local
        val isSame = uiState.value.postToQuote?.mentionedPost?.id == nostrId.hex
        if (isSame || !isPreparing.compareAndSet(false, true)) return@local
        _uiState.update { it.copy(postToQuote = null) }

        viewModelScope.launch(context = Dispatchers.IO) {
            preparePost(
                postToQuote = getCleanMentionedPost(postIdHex = nostrId.hex),
                relays = nostrId.recommendedRelays
            )
        }.invokeOnCompletion { isPreparing.set(false) }
    }

    val onToggleRelaySelection: (Int) -> Unit = { index ->
        val toggled = toggleRelay(relays = uiState.value.relayStatuses, index = index)
        if (toggled.any { it.isActive }) {
            _uiState.update { it.copy(relayStatuses = toggled) }
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
                list = uiState.value.relayStatuses,
                relaysUrlsToAdd = relayProvider.getReadRelaysOfPubkey(pubkey = pubkey)
            )
            _uiState.update { it.copy(relayStatuses = relaySelection) }
        }
    }

    val onSend: (String) -> Unit = { content ->
        viewModelScope.launch(context = Dispatchers.IO) {
            val event = sendPost(state = uiState.value, content = content)
            fullPostInserter.insertFullPost(events = listOf(event))
            dbExcludingCache.addPostIds(listOf(event.id))
        }
        resetUI()
    }

    private fun preparePost(
        postToQuote: AnnotatedMentionedPost?,
        relays: Collection<String> = emptyList()
    ) {
        _uiState.update {
            it.copy(
                relayStatuses = getRelayStatuses(),
                postToQuote = postToQuote,
                quoteRelays = relays
            )
        }
    }

    private suspend fun getCleanMentionedPost(postIdHex: String): AnnotatedMentionedPost? {
        val mentionedPost = postDao.getMentionedPost(postId = postIdHex) ?: return null
        val annotatedContent = annotatedContentHandler.annotateContent(
            content = mentionedPost.content,
            mentionedNamesByPubkey = emptyMap() // TODO: Get mentioned names
        )
        val annotatedMentionedPost = AnnotatedMentionedPost(
            annotatedContent = annotatedContent,
            mentionedPost = mentionedPost
        )
        if (annotatedMentionedPost.mentionedPost.name.isNullOrEmpty()) {
            val shortenedNpub = getShortenedNpubFromPubkey(mentionedPost.pubkey)
            return annotatedMentionedPost.copy(
                mentionedPost = annotatedMentionedPost.mentionedPost.copy(
                    name = shortenedNpub
                )
            )
        }

        return annotatedMentionedPost
    }

    private suspend fun sendPost(state: PostViewModelState, content: String): Event {
        val quote = getNewLineQuoteUri(
            postIdToQuote = state.postToQuote?.mentionedPost?.id,
            relays = state.quoteRelays
        )
        val post = postPreparer.getCleanPostWithTagsAndMentions(content = content + quote)
        val selectedRelays = state.relayStatuses
            .filter { it.isActive }
            .map { it.relayUrl }
        return nostrService.sendPost(
            content = post.content,
            mentions = post.mentions,
            hashtags = post.hashtags,
            relays = selectedRelays
        )
    }

    private fun resetUI() {
        _uiState.update {
            it.copy(
                relayStatuses = getRelayStatuses(),
                postToQuote = null,
                quoteRelays = emptyList(),
            )
        }
    }

    private fun getRelayStatuses() = listRelayStatuses(
        allRelayUrls = relayProvider.getWriteRelays(),
        relaySelection = AllRelays
    )

    private fun getNewLineQuoteUri(postIdToQuote: String?, relays: Collection<String>): String {
        return if (postIdToQuote == null) ""
        else createNeventUri(postId = postIdToQuote, relays = relays)
            ?.let { "\n\n$it" }
            .orEmpty()
    }

    companion object {
        fun provideFactory(
            pubkeyProvider: IPubkeyProvider,
            nostrService: INostrService,
            relayProvider: IRelayProvider,
            postPreparer: IPostPreparer,
            annotatedContentHandler: IAnnotatedContentHandler,
            fullPostInserter: FullPostInserter,
            dbExcludingCache: IIdCache,
            postDao: PostDao,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PostViewModel(
                    pubkeyProvider = pubkeyProvider,
                    nostrService = nostrService,
                    relayProvider = relayProvider,
                    postPreparer = postPreparer,
                    annotatedContentHandler = annotatedContentHandler,
                    fullPostInserter = fullPostInserter,
                    dbExcludingCache = dbExcludingCache,
                    postDao = postDao,
                ) as T
            }
        }
    }
}
