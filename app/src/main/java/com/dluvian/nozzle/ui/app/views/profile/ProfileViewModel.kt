package com.dluvian.nozzle.ui.app.views.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.profileIdToNostrId
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "ProfileViewModel"

// TODO: Sub contactlist of contacts if its your personal profile page. Only once tho

class ProfileViewModel(
    val postCardInteractor: IPostCardInteractor,
    val clickedMediaUrlCache: IClickedMediaUrlCache,
    private val feedProvider: IFeedProvider,
    private val profileProvider: IProfileWithMetaProvider,
    private val relayProvider: IRelayProvider,
    private val profileFollower: IProfileFollower,
    private val pubkeyProvider: IPubkeyProvider,
    private val contactListProvider: IContactListProvider,
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    var profileState: StateFlow<ProfileWithMeta> = MutableStateFlow(
        ProfileWithMeta.createEmpty()
    )

    // TODO: Check if this can replaced with SQL query and calling follow with Dispatchers.Main
    private val _isFollowedByMeState = MutableStateFlow(false)
    val isFollowedByMeState = _isFollowedByMeState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    private val paginator: IPaginator<PostWithMeta, CreatedAt> = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _isRefreshing.update { bool } },
        onGetPage = { lastCreatedAt, waitForSubscription ->
            val pubkey = profileState.value.pubkey
            feedProvider.getFeedFlow(
                feedSettings = getCurrentFeedSettings(pubkey = pubkey, relays = getRelays(pubkey)),
                limit = DB_BATCH_SIZE,
                until = lastCreatedAt,
                waitForSubscription = waitForSubscription
            )
        },
        onIdentifyLastParam = { post -> post?.entity?.createdAt ?: getCurrentTimeInSeconds() }
    )

    val feed = paginator.getList()

    private val recommendedRelays = mutableListOf<String>()

    private val isSettingPubkey = AtomicBoolean(false)
    val onSetProfileId: (String?) -> Unit = { profileId ->
        if (!isSettingPubkey.get()) {
            isSettingPubkey.set(true)
            val nonNullProfileId = profileId ?: pubkeyProvider.getActivePubkey()
            val nonNullPubkey = profileIdToNostrId(nonNullProfileId)?.hex ?: nonNullProfileId
            if (profileId.isNullOrEmpty()) Log.w(TAG, "Tried to set empty pubkey for UI")

            if (nonNullPubkey == profileState.value.pubkey) {
                Log.i(TAG, "Profile of $nonNullPubkey is already set. Do nothing")
                isSettingPubkey.set(false)
            } else {
                Log.i(TAG, "Set UI for $nonNullPubkey")
                followProcess = null
                viewModelScope.launch(context = Dispatchers.IO) {
                    setProfileAndFeed(profileId = nonNullProfileId)
                }.invokeOnCompletion {
                    isSettingPubkey.set(false)
                }
            }
        }
    }

    val onRefresh: () -> Unit = { paginator.refresh() }

    val onLoadMore: () -> Unit = { paginator.loadMore() }

    private var followProcess: Job? = null
    val onFollow: (String) -> Unit = { pubkeyToFollow ->
        if (!isFollowedByMeState.value) {
            followProcess?.cancel(CancellationException("Cancelled to start follow process"))
            _isFollowedByMeState.update { true }
            followProcess = viewModelScope.launch(context = Dispatchers.IO) {
                profileFollower.follow(pubkeyToFollow = pubkeyToFollow)
            }
            followProcess?.invokeOnCompletion { ex ->
                Log.i(TAG, "Follow process completed: error=${ex?.localizedMessage}")
            }
        }
    }

    val onUnfollow: (String) -> Unit = { pubkeyToUnfollow ->
        if (isFollowedByMeState.value) {
            followProcess?.cancel(CancellationException("Cancelled to start unfollow process"))
            _isFollowedByMeState.update { false }
            followProcess = viewModelScope.launch(context = Dispatchers.IO) {
                profileFollower.unfollow(pubkeyToUnfollow = pubkeyToUnfollow)
            }
            followProcess?.invokeOnCompletion { ex ->
                Log.i(TAG, "Unfollow process completed: error=${ex?.localizedMessage}")
            }
        }
    }

    private suspend fun setProfileAndFeed(profileId: String) {
        val nostrProfileId = profileIdToNostrId(profileId)
        val pubkey = nostrProfileId?.hex ?: profileId
        setProfile(profileId = profileId, pubkey = pubkey)
        setRecommendedRelays(recommended = nostrProfileId?.recommendedRelays.orEmpty())
        paginator.refresh()
    }

    private fun setRecommendedRelays(recommended: List<String>) {
        recommendedRelays.clear()
        recommendedRelays.addAll(recommended)
    }

    private suspend fun setProfile(profileId: String, pubkey: String) {
        Log.i(TAG, "Set profile of $profileId")
        _isFollowedByMeState.update {
            contactListProvider.listPersonalContactPubkeys().contains(pubkey)
        }
        profileState = profileProvider.getProfileFlow(profileId = profileId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                if (profileState.value.pubkey == pubkey) profileState.value
                else ProfileWithMeta.createEmpty(pubkey = pubkey),
            )
    }

    private fun getCurrentFeedSettings(pubkey: String, relays: List<String>): FeedSettings {
        return FeedSettings(
            isPosts = true,
            isReplies = true,
            hashtag = null,
            authorSelection = SingleAuthor(pubkey = pubkey),
            relaySelection = MultipleRelays(relays = relays)
        )
    }

    private suspend fun getRelays(pubkey: String): List<String> {
        // TODO: Refactor into util function. Same in ProfileWithAdditionalInfoProvider
        return recommendedRelays + relayProvider.getWriteRelaysOfPubkey(pubkey)
            .let {
                if (it.size > MAX_RELAYS) it.shuffled()
                    .sortedByDescending { relay -> relayProvider.getReadRelays().contains(relay) }
                    .take(MAX_RELAYS)
                else it
            }
            .ifEmpty { relayProvider.getReadRelays() }
    }

    companion object {
        fun provideFactory(
            postCardInteractor: IPostCardInteractor,
            profileFollower: IProfileFollower,
            feedProvider: IFeedProvider,
            relayProvider: IRelayProvider,
            profileProvider: IProfileWithMetaProvider,
            pubkeyProvider: IPubkeyProvider,
            clickedMediaUrlCache: IClickedMediaUrlCache,
            contactListProvider: IContactListProvider,
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
                        contactListProvider = contactListProvider,
                    ) as T
                }
            }
    }
}
