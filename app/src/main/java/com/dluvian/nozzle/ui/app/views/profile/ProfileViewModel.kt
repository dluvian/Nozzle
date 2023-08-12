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
import com.dluvian.nozzle.data.WAIT_TIME
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "ProfileViewModel"

class ProfileViewModel(
    private val feedProvider: IFeedProvider,
    private val profileProvider: IProfileWithAdditionalInfoProvider,
    private val relayProvider: IRelayProvider,
    private val profileFollower: IProfileFollower,
    private val postCardInteractor: IPostCardInteractor,
    private val pubkeyProvider: IPubkeyProvider,
    private val clickedMediaUrlCache: IClickedMediaUrlCache,
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
                delay(WAIT_TIME)
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

    private val isAppending = AtomicBoolean(false)
    val onLoadMore: () -> Unit = {
        if (!isAppending.get()) {
            isAppending.set(true)
            viewModelScope.launch(context = Dispatchers.IO) {
                Log.i(TAG, "Load more")
                val pubkey = profileState.value.pubkey
                appendFeed(
                    feedSettings = getCurrentFeedSettings(
                        pubkey = pubkey,
                        relays = getRelays(pubkey)
                    ),
                    dbBatchSize = DB_APPEND_BATCH_SIZE,
                )
                isAppending.set(false)
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

    // TODO: Refactor: Same in other ViewModels
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

    // TODO: Refactor: Same in other ViewModels
    val onQuote: (String) -> Unit = { id ->
        feedState.value.find { it.id == id }?.let {
            viewModelScope.launch(context = Dispatchers.IO) {
                TODO()
            }
        }
    }

    // TODO: Refactor: Same in other ViewModels
    val onShowMedia: (String) -> Unit = { mediaUrl ->
        clickedMediaUrlCache.insert(mediaUrl = mediaUrl)
    }

    // TODO: Refactor: Same in other ViewModels
    val onShouldShowMedia: (String) -> Boolean = { mediaUrl ->
        clickedMediaUrlCache.contains(mediaUrl = mediaUrl)
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
                SharingStarted.WhileSubscribed(),
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
            SharingStarted.WhileSubscribed(),
            if (profileState.value.pubkey == pubkey) feedState.value else emptyList(),
        )
        renewAdditionalDataSubscription(pubkey)
    }

    // TODO: Append in FeedProvider to reduce duplicate code in ProvileVM and FeedVM
    private suspend fun appendFeed(
        feedSettings: FeedSettings,
        dbBatchSize: Int,
    ) {
        feedState.value.lastOrNull()?.let { last ->
            Log.i(TAG, "Append feed")
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
                else emptyList()
            }
            .ifEmpty { getDefaultRelays() }
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
            clickedMediaUrlCache: IClickedMediaUrlCache,
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
                        clickedMediaUrlCache = clickedMediaUrlCache,
                        nostrSubscriber = nostrSubscriber,
                        nip65Dao = nip65Dao,
                        context = context,
                        clip = clip,
                    ) as T
                }
            }
    }
}
