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
import com.dluvian.nozzle.data.utils.SHORT_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDebounce
import com.dluvian.nozzle.data.utils.listRelayStatuses
import com.dluvian.nozzle.data.utils.toggleRelay
import com.dluvian.nozzle.model.AllRelays
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.RelaySelection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "PostViewModel"

data class PostViewModelState(
    val content: String = "",
    val pubkey: String = "",
    val relayStatuses: List<RelayActive> = emptyList(),
    val isSendable: Boolean = false,
)

class PostViewModel(
    private val personalProfileProvider: IPersonalProfileProvider,
    private val nostrService: INostrService,
    private val relayProvider: IRelayProvider,
    private val postDao: PostDao,
    context: Context,
) : ViewModel() {
    private val viewModelState = MutableStateFlow(PostViewModelState())

    var metadataState = personalProfileProvider.getMetadata()
        .distinctUntilChanged()
        .firstThenDebounce(SHORT_DEBOUNCE)
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
        Log.i(TAG, "Initialize PostViewModel")
    }

    val onPreparePost: (RelaySelection) -> Unit = { relaySelection ->
        metadataState = personalProfileProvider.getMetadata()
            .distinctUntilChanged()
            .firstThenDebounce(SHORT_DEBOUNCE)
            .stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                null
            )

        viewModelScope.launch(context = Dispatchers.IO) {
            Log.i(TAG, "Prepare new post")
            viewModelState.update {
                it.copy(
                    pubkey = personalProfileProvider.getPubkey(),
                    content = "",
                    isSendable = false,
                    relayStatuses = listRelayStatuses(
                        allRelayUrls = (relaySelection.getSelectedRelays()
                            .orEmpty() + relayProvider.getWriteRelays()
                            .toList()).distinct(),
                        relaySelection = AllRelays
                    ),
                )
            }
        }
    }

    val onChangeContent: (String) -> Unit = { input ->
        if (input != uiState.value.content) {
            viewModelState.update {
                it.copy(content = input, isSendable = input.isNotBlank())
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
            val err = getErrorText(context = context, state = state)
            if (err != null) {
                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
            } else {
                val selectedRelays = state.relayStatuses
                    .filter { it.isActive }
                    .map { it.relayUrl }
                    .ifEmpty { null }
                Log.i(TAG, "Send post to ${selectedRelays?.size ?: -1} relays")
                val event = nostrService.sendPost(
                    content = state.content,
                    relays = selectedRelays
                )
                viewModelScope.launch(context = Dispatchers.IO) {
                    postDao.insertIfNotPresent(PostEntity.fromEvent(event))
                }
                showPostPublishedToast()
                resetUI()
            }
        }
    }

    private val showPostPublishedToast: () -> Unit = {
        Toast.makeText(
            context,
            context.getString(R.string.post_published),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun getErrorText(context: Context, state: PostViewModelState): String? {
        return if (state.content.isBlank()) {
            context.getString(R.string.your_post_is_empty)
        } else if (state.relayStatuses.all { !it.isActive }) {
            context.getString(R.string.pls_select_relays)
        } else {
            null
        }
    }

    private fun resetUI() {
        viewModelState.update {
            it.copy(
                content = "",
                relayStatuses = listRelayStatuses(
                    allRelayUrls = relayProvider.getWriteRelays(),
                    relaySelection = AllRelays
                ),
                isSendable = false,
                pubkey = personalProfileProvider.getPubkey()
            )
        }
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
