package com.dluvian.nozzle.ui.app.views.post

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.MAX_SUGGESTION_LENGTH
import com.dluvian.nozzle.data.annotatedContent.IAnnotatedContentHandler
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.createNeventUri
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.postPreparer.IPostPreparer
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.ISimpleProfileProvider
import com.dluvian.nozzle.data.room.dao.HashtagDao
import com.dluvian.nozzle.data.room.dao.MentionDao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.utils.listRelayStatuses
import com.dluvian.nozzle.data.utils.toggleRelay
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.nostr.Event
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

const val TAG = "PostViewModel"
class PostViewModel(
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
    private val postPreparer: IPostPreparer,
    private val annotatedContentHandler: IAnnotatedContentHandler,
    private val simpleProfileProvider: ISimpleProfileProvider, // TODO: Move this to postPreparer
    private val postDao: PostDao,
    private val hashtagDao: HashtagDao,
    private val mentionDao: MentionDao,
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
        if (isPreparing.get() || uiState.value.postToQuote?.mentionedPost?.id == postIdToQuote) return@local
        val nostrId = nostrStrToNostrId(postIdToQuote) ?: return@local

        isPreparing.set(true)
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

    var debounceJob: Job? = null
    val onSearch: (String) -> Unit = { name ->
        debounceJob?.cancel(CancellationException("Start a new search"))
        debounceJob = viewModelScope.launch(Dispatchers.IO) {
            val suggestions = simpleProfileProvider.getSimpleProfiles(
                nameLike = name,
                limit = MAX_SUGGESTION_LENGTH
            )
            _uiState.update { it.copy(searchSuggestions = suggestions) }
        }
        debounceJob?.invokeOnCompletion {
            Log.i(TAG, "Completed search. Error = ${it?.localizedMessage}")
        }
    }

    val onClickMention: (Pubkey) -> Unit = { pubkey ->
        // TODO: Do it
        _uiState.update { it.copy(searchSuggestions = emptyList()) }

    }

    val onSend: (String) -> Unit = { content ->
        val event = sendPost(state = uiState.value, content = content)
        viewModelScope.launch(context = Dispatchers.IO) {
            postDao.insertWithHashtagsAndMentions(
                events = listOf(event),
                hashtagDao = hashtagDao,
                mentionDao = mentionDao,
            )

            // TODO: dbSweepExcludingCache.addPostId(event.id)
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

    private fun sendPost(state: PostViewModelState, content: String): Event {
        val quote = getNewLineQuoteUri(
            postIdToQuote = state.postToQuote?.mentionedPost?.id,
            relays = state.quoteRelays
        )
        val post =
            postPreparer.getCleanPostWithTagsAndMentions(content = content + quote)
        val selectedRelays = state.relayStatuses
            .filter { it.isActive }
            .map { it.relayUrl }
        return nostrService.sendPost(
            content = post.content,
            mentions = post.mentions,
            hashtags = post.hashtags,
            relays = selectedRelays // TODO: Add read relays of mentioned pubkeys
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
            simpleProfileProvider: ISimpleProfileProvider,
            postDao: PostDao,
            hashtagDao: HashtagDao,
            mentionDao: MentionDao,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PostViewModel(
                    pubkeyProvider = pubkeyProvider,
                    nostrService = nostrService,
                    relayProvider = relayProvider,
                    postPreparer = postPreparer,
                    annotatedContentHandler = annotatedContentHandler,
                    simpleProfileProvider = simpleProfileProvider,
                    postDao = postDao,
                    hashtagDao = hashtagDao,
                    mentionDao = mentionDao
                ) as T
            }
        }
    }
}
