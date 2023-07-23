package com.dluvian.nozzle.ui.app.views.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.getDefaultRelays
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.provider.IProfileWithAdditionalInfoProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "ProfileViewModel"
private const val DB_BATCH_SIZE = 25

class ProfileViewModel(
    private val feedProvider: IFeedProvider,
    private val profileProvider: IProfileWithAdditionalInfoProvider,
    private val relayProvider: IRelayProvider,
    private val profileFollower: IProfileFollower,
    private val postCardInteractor: IPostCardInteractor,
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrSubscriber: INostrSubscriber,
    private val nip65Dao: Nip65Dao,
    context: Context,
    clip: ClipboardManager,
) : ViewModel() {
    private val isRefreshingFlow = MutableStateFlow(false)
    val isRefreshingState = isRefreshingFlow
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    var profileState: StateFlow<ProfileWithAdditionalInfo> = MutableStateFlow(
        ProfileWithAdditionalInfo.createEmpty()
    )

    var feedState: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    init {
        Log.i(TAG, "Initialize ProfileViewModel")
        viewModelScope.launch(context = Dispatchers.IO) {
            setProfileAndFeed(
                pubkey = pubkeyProvider.getPubkey(),
                dbBatchSize = DB_BATCH_SIZE
            )
        }
    }

    private val isSettingPubkey = AtomicBoolean(false)
    val onSetPubkey: (String?) -> Unit = { pubkey ->
        if (!isSettingPubkey.get()) {
            isSettingPubkey.set(true)
            val nonNullPubkey = pubkey ?: pubkeyProvider.getPubkey()
            if (pubkey == null) Log.w(TAG, "Tried to set empty pubkey for UI")
            else Log.i(TAG, "Set UI for $pubkey")

            viewModelScope.launch(context = Dispatchers.IO) {
                setProfileAndFeed(pubkey = nonNullPubkey, dbBatchSize = DB_BATCH_SIZE)
                delay(1000)
            }.invokeOnCompletion {
                isSettingPubkey.set(false)
            }
        }
    }

    val onRefreshProfileView: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            Log.i(TAG, "Refresh profile view")
            isRefreshingFlow.update { true }
            setFeed(
                pubkey = profileState.value.pubkey,
                dbBatchSize = DB_BATCH_SIZE
            )
            delay(1000)
            isRefreshingFlow.update { false }
        }
    }

    val onLoadMore: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            Log.i(TAG, "Load more")
            val pubkey = profileState.value.pubkey
            appendFeed(
                feedSettings = getCurrentFeedSettings(pubkey = pubkey, relays = getRelays(pubkey)),
                dbBatchSize = DB_BATCH_SIZE,
            )
        }
    }

    val onCopyNpub: () -> Unit = {
        profileState.value.npub.let {
            Log.i(TAG, "Copy npub $it")
            clip.setText(AnnotatedString(it))
            Toast.makeText(context, context.getString(R.string.pubkey_copied), Toast.LENGTH_SHORT)
                .show()
        }
    }

    val onLike: (String) -> Unit = { id ->
        feedState.value.find { it.id == id }?.let {
            viewModelScope.launch(context = Dispatchers.IO) {
                postCardInteractor.like(
                    postId = id,
                    postPubkey = it.pubkey,
                    relays = relayProvider.getWriteRelays()
                )
            }
        }
    }

    val onRepost: (String) -> Unit = { id ->
        feedState.value.find { it.id == id }?.let {
            viewModelScope.launch(context = Dispatchers.IO) {
                postCardInteractor.repost(
                    postId = id,
                    postPubkey = it.pubkey,
                    originUrl = it.relays.firstOrNull().orEmpty(),
                    relays = relayProvider.getWriteRelays()
                )
            }
        }
    }

    private val isInFollowProcess = AtomicBoolean(false)
    val onFollow: (String) -> Unit = { pubkeyToFollow ->
        if (!isInFollowProcess.get() && !profileState.value.isFollowedByMe) {
            isInFollowProcess.set(true)
            viewModelScope.launch(context = Dispatchers.IO) {
                // TODO: Set best relayUrl, not random
                profileFollower.follow(
                    pubkeyToFollow = pubkeyToFollow,
                    relayUrl = profileState.value.relays.randomOrNull().orEmpty()
                )
            }.invokeOnCompletion {
                isInFollowProcess.set(false)
            }
        }
    }

    val onUnfollow: (String) -> Unit = { pubkeyToUnfollow ->
        if (!isInFollowProcess.get() && profileState.value.isFollowedByMe) {
            isInFollowProcess.set(true)
            viewModelScope.launch(context = Dispatchers.IO) {
                profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
            }.invokeOnCompletion {
                isInFollowProcess.set(false)
            }
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun setProfileAndFeed(pubkey: String, dbBatchSize: Int) {
        Log.i(TAG, "Set profile of $pubkey")
        profileState = profileProvider.getProfileFlow(pubkey)
            .firstThenDebounce(SHORT_DEBOUNCE)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(replayExpirationMillis = 0),
                ProfileWithAdditionalInfo.createEmpty(),
            )
        setFeed(pubkey = pubkey, dbBatchSize = dbBatchSize)
    }

    @OptIn(FlowPreview::class)
    private suspend fun setFeed(pubkey: String, dbBatchSize: Int) {
        Log.i(TAG, "Set feed of $pubkey")
        feedState = feedProvider.getFeedFlow(
            feedSettings = getCurrentFeedSettings(pubkey = pubkey, relays = getRelays(pubkey)),
            limit = dbBatchSize
        ).firstThenDebounce(SHORT_DEBOUNCE)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                if (profileState.value.pubkey == pubkey) feedState.value else emptyList(),
            )
        renewAdditionalDataSubscription(pubkey)
    }

    private val isAppending = AtomicBoolean(false)

    private suspend fun appendFeed(
        feedSettings: FeedSettings,
        dbBatchSize: Int,
    ) {
        if (isAppending.get()) return

        feedState.value.lastOrNull()?.let { last ->
            Log.i(TAG, "Append feed")
            isAppending.set(true)
            feedState = feedProvider.getFeedFlow(
                feedSettings = feedSettings,
                limit = dbBatchSize,
                until = last.createdAt
            ).map { toAppend -> feedState.value + toAppend }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    feedState.value,
                )
            isAppending.set(false)
            renewAdditionalDataSubscription(pubkey = profileState.value.pubkey)
        }
    }

    // TODO: Handle sub in FeedProvider
    private suspend fun renewAdditionalDataSubscription(pubkey: String) {
        nostrSubscriber.unsubscribeAdditionalPostsData()
        nostrSubscriber.subscribeToAdditionalPostsData(
            posts = feedState.value.takeLast(DB_BATCH_SIZE),
            relays = getRelays(pubkey)
        )
    }

    private fun getCurrentFeedSettings(pubkey: String, relays: List<String>): FeedSettings {
        return FeedSettings(
            isPosts = true,
            isReplies = true,
            authorSelection = SingleAuthor(pubkey = pubkey),
            relaySelection = MultipleRelays(relays = relays)
        )
    }

    private suspend fun getRelays(pubkey: String): List<String> {
        return nip65Dao.getWriteRelaysOfPubkey(pubkey)
            .ifEmpty {
                if (profileState.value.pubkey == pubkey) profileState.value.relays
                else getDefaultRelays()
            }
            .shuffled()
            .take(5)  // Don't ask more than 5 relays
    }

    companion object {
        fun provideFactory(
            profileFollower: IProfileFollower,
            postCardInteractor: IPostCardInteractor,
            feedProvider: IFeedProvider,
            relayProvider: IRelayProvider,
            profileProvider: IProfileWithAdditionalInfoProvider,
            pubkeyProvider: IPubkeyProvider,
            nostrSubscriber: INostrSubscriber,
            nip65Dao: Nip65Dao,
            context: Context,
            clip: ClipboardManager,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(
                        profileFollower = profileFollower,
                        postCardInteractor = postCardInteractor,
                        feedProvider = feedProvider,
                        profileProvider = profileProvider,
                        pubkeyProvider = pubkeyProvider,
                        relayProvider = relayProvider,
                        nostrSubscriber = nostrSubscriber,
                        nip65Dao = nip65Dao,
                        context = context,
                        clip = clip,
                    ) as T
                }
            }
    }
}
