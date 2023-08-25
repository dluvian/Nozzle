package com.dluvian.nozzle.ui.app.views.post

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.nostr.INostrService
import com.dluvian.nozzle.data.provider.IPersonalProfileProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.data.utils.hexToNote
import com.dluvian.nozzle.data.utils.listRelayStatuses
import com.dluvian.nozzle.data.utils.toggleRelay
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.MentionedPost
import com.dluvian.nozzle.model.RelayActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "PostViewModel"

data class PostViewModelState(
    val content: String = "",
    val pubkey: String = "",
    val relayStatuses: List<RelayActive> = emptyList(),
    val isSendable: Boolean = false,
    val postToQuote: MentionedPost? = null,
)

class PostViewModel(
    private val personalProfileProvider: IPersonalProfileProvider,
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
    private val postDao: PostDao,
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

    init {
        Log.i(TAG, "Initialize PostViewModel")
    }

    val onPreparePost: () -> Unit = {
        preparePost(postToQuote = null)
    }

    // TODO: Add recipients read relays to selection
    private val isPreparing = AtomicBoolean(false)
    val onPrepareQuote: (String) -> Unit = { postIdToQuote ->
        if (!isPreparing.get() && uiState.value.postToQuote?.id != postIdToQuote) {
            Log.i(TAG, "Prepare quoting $postIdToQuote")
            isPreparing.set(true)
            viewModelState.update { it.copy(postToQuote = null) }
            viewModelScope.launch(context = Dispatchers.IO) {
                val postToQuote = postDao.getNullableMentionedPost(postId = postIdToQuote)
                preparePost(postToQuote = postToQuote?.toMentionedPost())
            }.invokeOnCompletion { isPreparing.set(false) }
        }
    }

    private fun preparePost(postToQuote: MentionedPost?) {
        updateMetadataState()
        Log.i(TAG, "Prepare new ${postToQuote?.let { "quoted " } ?: ""}post")
        viewModelState.update {
            it.copy(
                pubkey = personalProfileProvider.getPubkey(),
                content = "",
                isSendable = postToQuote != null,
                relayStatuses = getRelayStatuses(),
                postToQuote = postToQuote
            )
        }
    }

    val onChangeContent: (String) -> Unit = { input ->
        if (input != uiState.value.content) {
            viewModelState.update {
                it.copy(content = input, isSendable = input.isNotBlank() || it.postToQuote != null)
            }
        }
    }

    val onToggleRelaySelection: (Int) -> Unit = { index ->
        val toggled = toggleRelay(relays = uiState.value.relayStatuses, index = index)
        if (toggled.any { it.isActive }) {
            viewModelState.update {
                it.copy(relayStatuses = toggled)
            }
        }
    }

    val onSend: () -> Unit = {
        uiState.value.let { state ->
            val selectedRelays = state.relayStatuses
                .filter { it.isActive }
                .map { it.relayUrl }
            Log.i(TAG, "Send post to ${selectedRelays.size} relays")
            val event = nostrService.sendPost(
                content = getContentToPublish(state = state),
                relays = selectedRelays
            )
            viewModelScope.launch(context = Dispatchers.IO) {
                postDao.insertIfNotPresent(PostEntity.fromEvent(event))
            }
            showPostPublishedToast()
            resetUI()
        }
    }

    private fun resetUI() {
        Log.i(TAG, "Reset Post view")
        viewModelState.update {
            it.copy(
                content = "",
                relayStatuses = getRelayStatuses(),
                isSendable = false,
                pubkey = personalProfileProvider.getPubkey(),
                postToQuote = null
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

    private fun getContentToPublish(state: PostViewModelState): String {
        return (state.content + getNewLineQuoteUri(state.postToQuote?.id)).trim()
    }


    // TODO: Move to utils
    private fun getNewLineQuoteUri(postIdToQuote: String?): String {
        return if (postIdToQuote == null) ""
        // TODO: nostr: URI from utils
        else hexToNote(postId = postIdToQuote).let { noteId -> "\nnostr:$noteId" }
    }

    companion object {
        fun provideFactory(
            personalProfileProvider: IPersonalProfileProvider,
            nostrService: INostrService,
            relayProvider: IRelayProvider,
            postDao: PostDao,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PostViewModel(
                    nostrService = nostrService,
                    personalProfileProvider = personalProfileProvider,
                    postDao = postDao,
                    relayProvider = relayProvider,
                    context = context,
                ) as T
            }
        }
    }
}
