package com.dluvian.nozzle.ui.app.views.profile

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nostrclientkt.model.Metadata
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
    private val isRefreshing = MutableStateFlow(false)
    val isRefreshingState = isRefreshing
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            false
        )

    // TODO: Figure out how to do it without this hack
    private val forceRecomposition = MutableStateFlow(0)
    val forceRecompositionState = forceRecomposition
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            0
        )

    var profileState: StateFlow<ProfileWithAdditionalInfo> = MutableStateFlow(
        ProfileWithAdditionalInfo(
            pubkey = "",
            npub = "",
            metadata = Metadata(),
            numOfFollowing = 0,
            numOfFollowers = 0,
            relays = listOf(),
            isOneself = false,
            isFollowedByMe = false,
        )
    )

    var feedState: StateFlow<List<PostWithMeta>> = MutableStateFlow(listOf())

    init {
        Log.i(TAG, "Initialize ProfileViewModel")
        viewModelScope.launch(context = Dispatchers.IO) {
            refreshProfileAndPostState(
                pubkey = pubkeyProvider.getPubkey(),
                dbBatchSize = DB_BATCH_SIZE
            )
        }
    }

    val onSetPubkey: (String?) -> Unit = { pubkey ->
        viewModelScope.launch(context = Dispatchers.IO) {
            if (pubkey == null) {
                Log.w(TAG, "Tried to set empty pubkey for UI")
                refreshProfileAndPostState(
                    pubkey = pubkeyProvider.getPubkey(),
                    dbBatchSize = DB_BATCH_SIZE
                )
            } else {
                Log.i(TAG, "Set UI for $pubkey")
                refreshProfileAndPostState(pubkey = pubkey, dbBatchSize = DB_BATCH_SIZE)
            }
        }
    }

    val onRefreshProfileView: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            Log.i(TAG, "Refresh profile view")
            setUIRefresh(true)
            refreshPostState(
                pubkey = profileState.value.pubkey,
                dbBatchSize = DB_BATCH_SIZE
            )
            delay(1000)
            setUIRefresh(false)
        }
    }

    val onLoadMore: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            Log.i(TAG, "Load more")
            appendFeed(
                currentFeed = feedState.value,
                feedSettings = getCurrentFeedSettings(),
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

    private var isInFollowProcess = AtomicBoolean(false)

    val onFollow: (String) -> Unit = { pubkeyToFollow ->
        if (!profileState.value.isFollowedByMe && !isInFollowProcess.get()) {
            isInFollowProcess.set(true)
            viewModelScope.launch(context = Dispatchers.IO) {
                profileFollower.follow(pubkeyToFollow = pubkeyToFollow, relayUrl = "")
            }.invokeOnCompletion { isInFollowProcess.set(false) }
        }
    }

    val onUnfollow: (String) -> Unit = { pubkeyToUnfollow ->
        if (profileState.value.isFollowedByMe && !isInFollowProcess.get()) {
            isInFollowProcess.set(true)
            viewModelScope.launch(context = Dispatchers.IO) {
                profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
            }.invokeOnCompletion { isInFollowProcess.set(false) }
        }
    }

    private suspend fun refreshProfileAndPostState(pubkey: String, dbBatchSize: Int) {
        Log.i(TAG, "Refresh profile and posts of $pubkey")
        profileState = profileProvider.getProfileFlow(pubkey)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                profileState.value,
            )
        refreshPostState(pubkey = pubkey, dbBatchSize = dbBatchSize)
    }

    private suspend fun refreshPostState(pubkey: String, dbBatchSize: Int) {
        Log.i(TAG, "Refresh posts of $pubkey")
        feedState = feedProvider.getFeedFlow(
            feedSettings = getCurrentFeedSettings(),
            limit = dbBatchSize
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            if (pubkey == profileState.value.pubkey) feedState.value else listOf(),
        )
        forceRecomposition.update { it + 1 }
        renewAdditionalDataSubscription()
    }

    private val isAppending = AtomicBoolean(false)

    private suspend fun appendFeed(
        currentFeed: List<PostWithMeta>,
        feedSettings: FeedSettings,
        dbBatchSize: Int,
    ) {
        if (isAppending.get()) return

        Log.i(TAG, "Append feed")
        currentFeed.lastOrNull()?.let { last ->
            isAppending.set(true)
            feedState = feedProvider.getFeedFlow(
                feedSettings = feedSettings,
                limit = dbBatchSize,
                until = last.createdAt
            ).map { toAppend -> currentFeed + toAppend }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(),
                    currentFeed,
                )
            isAppending.set(false)
            forceRecomposition.update { it + 1 }
        }
        renewAdditionalDataSubscription()
    }

    private suspend fun renewAdditionalDataSubscription() {
        nostrSubscriber.unsubscribeAdditionalPostsData()
        nostrSubscriber.subscribeToAdditionalPostsData(
            posts = feedState.value.takeLast(DB_BATCH_SIZE),
            relays = getRelays()
        )
    }

    private fun setUIRefresh(value: Boolean) {
        isRefreshing.update { value }
    }

    private suspend fun getCurrentFeedSettings(): FeedSettings {
        return FeedSettings(
            isPosts = true,
            isReplies = true,
            authorSelection = SingleAuthor(profileState.value.pubkey),
            relaySelection = MultipleRelays(relays = getRelays())
        )
    }

    private suspend fun getRelays(): List<String> {
        return nip65Dao.getWriteRelaysOfPubkey(profileState.value.pubkey)
            .ifEmpty {
                profileState.value
                    .relays
                    .ifEmpty { getDefaultRelays() }
            }.shuffled()
            .take(10)  // Don't ask more than 10 relays
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
