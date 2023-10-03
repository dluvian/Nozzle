package com.dluvian.nozzle.ui.app.views.post

import android.content.Context
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.createNeventUri
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.data.nostr.utils.MentionUtils.getCleanPostWithMentions
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpubFromPubkey
import com.dluvian.nozzle.data.provider.IPersonalProfileProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.HashtagDao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.entity.HashtagEntity
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.utils.HashtagUtils
import com.dluvian.nozzle.data.utils.listRelayStatuses
import com.dluvian.nozzle.data.utils.toggleRelay
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.nostr.Event
import com.dluvian.nozzle.model.nostr.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

data class PostViewModelState(
    val content: String = "",
    val pubkey: String = "",
    val relayStatuses: List<RelayActive> = emptyList(),
    val isSendable: Boolean = false,
    val postToQuote: MentionedPost? = null,
    val quoteRelays: Collection<String> = emptyList(),
)

class PostViewModel(
    private val personalProfileProvider: IPersonalProfileProvider,
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
    private val postDao: PostDao,
    private val hashtagDao: HashtagDao,
    context: Context,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(PostViewModelState())

    var metadataState = personalProfileProvider.getMetadataStateFlow()

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            viewModelState.value
        )

    val onPreparePost: () -> Unit = {
        preparePost(postToQuote = null)
    }

    private val isPreparing = AtomicBoolean(false)
    val onPrepareQuote: (String) -> Unit = local@{ postIdToQuote ->
        if (isPreparing.get() || uiState.value.postToQuote?.id == postIdToQuote) return@local
        val nostrId = nostrStrToNostrId(postIdToQuote) ?: return@local

        isPreparing.set(true)
        viewModelState.update { it.copy(postToQuote = null) }
        viewModelScope.launch(context = Dispatchers.IO) {
            val postToQuote = getCleanMentionedPost(postIdHex = nostrId.hex)
            preparePost(postToQuote = postToQuote, relays = nostrId.recommendedRelays)
        }.invokeOnCompletion { isPreparing.set(false) }
    }

    val onChangeContent: (String) -> Unit = local@{ input ->
        if (input == uiState.value.content) return@local
        viewModelState.update {
            it.copy(content = input, isSendable = input.isNotBlank() || it.postToQuote != null)
        }
    }

    val onToggleRelaySelection: (Int) -> Unit = { index ->
        val toggled = toggleRelay(relays = uiState.value.relayStatuses, index = index)
        if (toggled.any { it.isActive }) {
            viewModelState.update { it.copy(relayStatuses = toggled) }
        }
    }

    val onSend: () -> Unit = {
        val event = sendPost(state = uiState.value)
        viewModelScope.launch(context = Dispatchers.IO) {
            postDao.insertIfNotPresent(PostEntity.fromEvent(event))
            // TODO: Insert hashtags in tx
            // TODO: dbSweepExcludingCache.addPostId(event.id)
            val hashtags = event.getHashtags()
                .map { HashtagEntity(eventId = event.id, hashtag = it) }
            if (hashtags.isNotEmpty()) {
                hashtagDao.insertOrIgnore(*hashtags.toTypedArray())
            }
        }
        showPostPublishedToast()
        resetUI()
    }

    private fun preparePost(postToQuote: MentionedPost?, relays: Collection<String> = emptyList()) {
        updateMetadataState()
        viewModelState.update {
            it.copy(
                pubkey = personalProfileProvider.getPubkey(),
                content = "",
                isSendable = postToQuote != null,
                relayStatuses = getRelayStatuses(),
                postToQuote = postToQuote,
                quoteRelays = relays
            )
        }
    }

    private suspend fun getCleanMentionedPost(postIdHex: String): MentionedPost? {
        val mentionedPost = postDao.getMentionedPost(postId = postIdHex) ?: return null
        if (mentionedPost.name.isNullOrEmpty()) {
            val shortenedNpub = getShortenedNpubFromPubkey(mentionedPost.pubkey)
            return mentionedPost.copy(name = shortenedNpub)
        }

        return mentionedPost
    }

    private fun sendPost(state: PostViewModelState): Event {
        val post = getContentToPublish(state = state)
        val selectedRelays = state.relayStatuses
            .filter { it.isActive }
            .map { it.relayUrl }
        return nostrService.sendPost(
            content = post.content,
            mentions = post.mentions,
            hashtags = HashtagUtils.extractHashtagValues(post.content),
            relays = selectedRelays
        )
    }

    private fun resetUI() {
        viewModelState.update {
            it.copy(
                content = "",
                relayStatuses = getRelayStatuses(),
                isSendable = false,
                pubkey = personalProfileProvider.getPubkey(),
                postToQuote = null,
                quoteRelays = emptyList(),
            )
        }
    }

    // TODO: USE FLOWS. This should not be needed
    private fun updateMetadataState() {
        metadataState = personalProfileProvider.getMetadataStateFlow()
    }

    private val showPostPublishedToast: () -> Unit = {
        Toast.makeText(
            context,
            context.getString(R.string.post_published),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getRelayStatuses() = listRelayStatuses(
        allRelayUrls = relayProvider.getWriteRelays(),
        relaySelection = AllRelays
    )

    private fun getContentToPublish(state: PostViewModelState): Post {
        val quote = getNewLineQuoteUri(
            postIdToQuote = state.postToQuote?.id,
            relays = state.quoteRelays
        )
        val postWithMentions = getCleanPostWithMentions(content = state.content)

        return postWithMentions.copy(content = postWithMentions.content + quote)
    }

    private fun getNewLineQuoteUri(postIdToQuote: String?, relays: Collection<String>): String {
        return if (postIdToQuote == null) ""
        else createNeventUri(postId = postIdToQuote, relays = relays)
            ?.let { "\n\n$it" }
            .orEmpty()
    }

    companion object {
        fun provideFactory(
            personalProfileProvider: IPersonalProfileProvider,
            nostrService: INostrService,
            relayProvider: IRelayProvider,
            postDao: PostDao,
            hashtagDao: HashtagDao,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PostViewModel(
                    nostrService = nostrService,
                    personalProfileProvider = personalProfileProvider,
                    relayProvider = relayProvider,
                    postDao = postDao,
                    hashtagDao = hashtagDao,
                    context = context,
                ) as T
            }
        }
    }
}
