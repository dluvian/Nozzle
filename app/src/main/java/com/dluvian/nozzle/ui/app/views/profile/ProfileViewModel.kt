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
import com.dluvian.nozzle.data.DB_APPEND_BATCH_SIZE
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.MAX_FEED_LENGTH
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.nostr.INostrSubscriber
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.provider.IProfileWithAdditionalInfoProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.utils.hasUnknownReferencedAuthors
import com.dluvian.nozzle.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

private const val TAG = "ProfileViewModel"

// TODO: Sub contactlist of contacts if its your personal profile page. Only once tho

class ProfileViewModel(
    val postCardInteractor: IPostCardInteractor,
    val clickedMediaUrlCache: IClickedMediaUrlCache,
    private val feedProvider: IFeedProvider,
    private val profileProvider: IProfileWithAdditionalInfoProvider,
    private val relayProvider: IRelayProvider,
    private val profileFollower: IProfileFollower,
    private val pubkeyProvider: IPubkeyProvider,
    private val nostrSubscriber: INostrSubscriber,
    context: Context,
    clip: ClipboardManager,
) : ViewModel() {
    private val isRefreshingFlow = MutableStateFlow(false)
    val isRefreshingState = isRefreshingFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    var profileState: StateFlow<ProfileWithAdditionalInfo> = MutableStateFlow(
        ProfileWithAdditionalInfo.createEmpty()
    )

    var feedState: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    init {
        Log.i(TAG, "Initialize ProfileViewModel")
    }

    private val failedAppendAttempts = AtomicInteger(0)

    private val isSettingPubkey = AtomicBoolean(false)
    val onSetPubkey: (String?) -> Unit = { pubkey ->
        if (!isSettingPubkey.get()) {
            isSettingPubkey.set(true)
            val nonNullPubkey = pubkey ?: pubkeyProvider.getPubkey()

            if (pubkey == null) Log.w(TAG, "Tried to set empty pubkey for UI")
            if (pubkey == profileState.value.pubkey) {
                Log.i(TAG, "Profile of $pubkey is already set. Do nothing")
                isSettingPubkey.set(false)
            } else {
                Log.i(TAG, "Set UI for $pubkey")
                failedAppendAttempts.set(0)
                viewModelScope.launch(context = Dispatchers.IO) {
                    setProfileAndFeed(pubkey = nonNullPubkey, dbBatchSize = DB_BATCH_SIZE)
                }.invokeOnCompletion {
                    isSettingPubkey.set(false)
                }
            }
        }
    }

    val onRefreshProfileView: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            Log.i(TAG, "Refresh profile view")
            isRefreshingFlow.update { true }
            failedAppendAttempts.set(0)
            setFeed(
                pubkey = profileState.value.pubkey,
                dbBatchSize = DB_BATCH_SIZE
            )
            delay(1000)
            isRefreshingFlow.update { false }
        }
    }

    private val isAppending = AtomicBoolean(false)
    val onLoadMore: () -> Unit = {
        // TODO: Magic number
        if (!isAppending.get() && failedAppendAttempts.get() < 5) {
            isAppending.set(true)
            viewModelScope.launch(context = Dispatchers.IO) {
                Log.i(TAG, "Load more")
                val pubkey = profileState.value.pubkey
                val currentFeed = feedState.value
                appendFeed(pubkey = pubkey, currentFeed = currentFeed)
                delay(WAIT_TIME)
                if (currentFeed.lastOrNull()?.id.orEmpty() == feedState.value.lastOrNull()?.id.orEmpty()) {
                    val attempt = failedAppendAttempts.getAndIncrement()
                    Log.w(TAG, "Failed to append profile feed. Attempt $attempt")
                }
                isAppending.set(false)
                renewReferencedDataSubscription(pubkey)
            }
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

    private val isInFollowProcess = AtomicBoolean(false)
    val onFollow: (String) -> Unit = { pubkeyToFollow ->
        if (!isInFollowProcess.get() && !profileState.value.isFollowedByMe) {
            isInFollowProcess.set(true)
            viewModelScope.launch(context = Dispatchers.IO) {
                profileFollower.follow(
                    pubkeyToFollow = pubkeyToFollow
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

    private suspend fun setProfileAndFeed(pubkey: String, dbBatchSize: Int) {
        Log.i(TAG, "Set profile of $pubkey")
        profileState = profileProvider.getProfileFlow(pubkey)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                ProfileWithAdditionalInfo.createEmpty(),
            )
        setFeed(pubkey = pubkey, dbBatchSize = dbBatchSize)
    }

    private suspend fun setFeed(pubkey: String, dbBatchSize: Int) {
        Log.i(TAG, "Set feed of $pubkey")
        feedState = feedProvider.getFeedFlow(
            feedSettings = getCurrentFeedSettings(pubkey = pubkey, relays = getRelays(pubkey)),
            limit = dbBatchSize
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
            if (profileState.value.pubkey == pubkey) feedState.value else emptyList(),
        )
        delay(WAIT_TIME)
        renewReferencedDataSubscription(pubkey)
    }

    // TODO: Append in FeedProvider to reduce duplicate code in ProvileVM and FeedVM
    private suspend fun appendFeed(pubkey: String, currentFeed: List<PostWithMeta>) {
        isRefreshingFlow.update { (true) }
        feedState.value.lastOrNull()?.let { last ->
            feedState = feedProvider.getFeedFlow(
                feedSettings = getCurrentFeedSettings(
                    pubkey = pubkey,
                    relays = getRelays(pubkey)
                ),
                limit = DB_APPEND_BATCH_SIZE,
                until = last.createdAt
            ).distinctUntilChanged()
                .map { toAppend -> currentFeed.takeLast(MAX_FEED_LENGTH) + toAppend }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                    currentFeed,
                )
        }
        delay(WAIT_TIME)
        isRefreshingFlow.update { (false) }
    }

    // TODO: Handle sub in FeedProvider. This is copy&paste from FeedVM
    private val isRenewingRef = AtomicBoolean(false)
    private suspend fun renewReferencedDataSubscription(pubkey: String) {
        if (isRenewingRef.get()) return
        isRenewingRef.set(true)
        nostrSubscriber.subscribeToReferencedData(
            posts = feedState.value.takeLast(DB_BATCH_SIZE),
            relays = getRelays(pubkey)
        )

        delay(2 * WAIT_TIME)
        if (isAppending.get() || isRefreshingFlow.value) {
            isRenewingRef.set(false)
            return
        }

        val postsWithUnknowns = feedState.value
            .takeLast(DB_BATCH_SIZE)
            .filter { hasUnknownReferencedAuthors(it) }
        if (postsWithUnknowns.isNotEmpty()) {
            Log.i(TAG, "Resubscribe missing posts and profiles of ${postsWithUnknowns.size} posts")
            nostrSubscriber.unsubscribeReferencedPostsData()
            nostrSubscriber.subscribeToReferencedData(
                posts = postsWithUnknowns,
                relays = null
            )
        }
        isRenewingRef.set(false)
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
        return relayProvider.getWriteRelaysOfPubkey(pubkey)
            .ifEmpty { relayProvider.getReadRelays() }
    }

    companion object {
        fun provideFactory(
            postCardInteractor: IPostCardInteractor,
            profileFollower: IProfileFollower,
            feedProvider: IFeedProvider,
            relayProvider: IRelayProvider,
            profileProvider: IProfileWithAdditionalInfoProvider,
            pubkeyProvider: IPubkeyProvider,
            clickedMediaUrlCache: IClickedMediaUrlCache,
            nostrSubscriber: INostrSubscriber,
            context: Context,
            clip: ClipboardManager,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(
                        postCardInteractor = postCardInteractor,
                        clickedMediaUrlCache = clickedMediaUrlCache,
                        feedProvider = feedProvider,
                        profileProvider = profileProvider,
                        relayProvider = relayProvider,
                        profileFollower = profileFollower,
                        pubkeyProvider = pubkeyProvider,
                        nostrSubscriber = nostrSubscriber,
                        context = context,
                        clip = clip,
                    ) as T
                }
            }
    }
}
