package com.dluvian.nozzle.ui.app.views.reply

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
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDebounce
import com.dluvian.nozzle.data.utils.listRelayStatuses
import com.dluvian.nozzle.data.utils.toggleRelay
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.nostr.ReplyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ReplyViewModel"

data class ReplyViewModelState(
    val recipientName: String = "",
    val reply: String = "",
    val isSendable: Boolean = false,
    val pubkey: String = "",
    val relaySelection: List<RelayActive> = emptyList(),
)

class ReplyViewModel(
    private val nostrService: INostrService,
    private val personalProfileProvider: IPersonalProfileProvider,
    private val relayProvider: IRelayProvider,
    private val postDao: PostDao,
    context: Context,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(ReplyViewModelState())
    private var recipientPubkey: String = ""
    private var postToReplyTo: PostWithMeta? = null

    var metadataState = personalProfileProvider.getMetadata()
        .firstThenDebounce(NORMAL_DEBOUNCE)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    val uiState = viewModelState
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            viewModelState.value
        )

    init {
        Log.i(TAG, "Initialize ReplyViewModel")
    }

    val onPrepareReply: (PostWithMeta) -> Unit = { post ->
        metadataState = personalProfileProvider.getMetadata()
            .firstThenDebounce(NORMAL_DEBOUNCE)
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                null
            )

        postToReplyTo = post
        viewModelScope.launch(context = Dispatchers.IO) {
            Log.i(TAG, "Set reply to ${post.pubkey}")
            viewModelState.update {
                recipientPubkey = post.pubkey
                it.copy(
                    recipientName = post.name,
                    pubkey = personalProfileProvider.getPubkey(),
                    reply = "",
                    isSendable = false,
                    relaySelection = listRelayStatuses(
                        allRelayUrls = (post.relays + relayProvider.getWriteRelays()).distinct(),
                        relaySelection = AllRelays,
                    ),
                )
            }
        }
    }

    val onChangeReply: (String) -> Unit = { input ->
        if (input != uiState.value.reply) {
            viewModelState.update {
                it.copy(reply = input, isSendable = input.isNotBlank())
            }
        }
    }

    val onToggleRelaySelection: (Int) -> Unit = { index ->
        val toggled = toggleRelay(relays = uiState.value.relaySelection, index = index)
        if (toggled.any { it.isActive }) {
            viewModelState.update {
                it.copy(relaySelection = toggled)
            }
        }
    }

    val onSend: () -> Unit = {
        uiState.value.let { state ->
            val err = getErrorText(context = context, state = state)
            if (err != null) {
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            } else {
                postToReplyTo?.let { parentPost ->
                    Log.i(TAG, "Send reply to ${state.recipientName} ${state.pubkey}")
                    val replyTo = ReplyTo(
                        replyToRoot = parentPost.replyToRootId,
                        replyTo = parentPost.id,
                        relayUrl = parentPost.relays.firstOrNull().orEmpty(),
                    )
                    val selectedRelays = state.relaySelection
                        .filter { it.isActive }
                        .map { it.relayUrl }
                        .ifEmpty { null }
                    val event = nostrService.sendReply(
                        replyTo = replyTo,
                        replyToPubkey = parentPost.pubkey,
                        content = state.reply,
                        relays = selectedRelays
                    )
                    viewModelScope.launch(context = Dispatchers.IO) {
                        postDao.insertIfNotPresent(PostEntity.fromEvent(event))
                    }
                }
                resetUI()
            }
        }
    }

    private fun getErrorText(context: Context, state: ReplyViewModelState): String? {
        return if (state.reply.isBlank()) {
            context.getString(R.string.your_reply_is_empty)
        } else if (state.relaySelection.all { !it.isActive }) {
            context.getString(R.string.pls_select_relays)
        } else {
            null
        }
    }

    private fun resetUI() {
        viewModelState.update {
            recipientPubkey = ""
            it.copy(
                recipientName = "",
                reply = "",
                isSendable = false,
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
            personalProfileProvider: IPersonalProfileProvider,
            relayProvider: IRelayProvider,
            postDao: PostDao,
            context: Context
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReplyViewModel(
                    nostrService = nostrService,
                    personalProfileProvider = personalProfileProvider,
                    relayProvider = relayProvider,
                    postDao = postDao,
                    context = context,
                ) as T
            }
        }
    }
}
