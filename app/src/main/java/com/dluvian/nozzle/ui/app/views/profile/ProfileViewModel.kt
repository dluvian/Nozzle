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
import com.dluvian.nozzle.data.APPEND_RETRY_TIME
import com.dluvian.nozzle.data.DB_APPEND_BATCH_SIZE
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.MAX_APPEND_ATTEMPTS
import com.dluvian.nozzle.data.MAX_FEED_LENGTH
import com.dluvian.nozzle.data.MAX_RELAYS
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.profileIdToNostrId
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IFeedProvider
import com.dluvian.nozzle.data.provider.IProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

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

    var profileState: StateFlow<ProfileWithMeta> = MutableStateFlow(
        ProfileWithMeta.createEmpty()
    )

    var feedState: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    private val isFollowedByMeFlow = MutableStateFlow(false)
    val isFollowedByMeState = isFollowedByMeFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    private val recommendedRelays = mutableListOf<String>()

    private val failedAppendAttempts = AtomicInteger(0)

    private val isSettingPubkey = AtomicBoolean(false)
    val onSetProfileId: (String?) -> Unit = { profileId ->
        if (!isSettingPubkey.get()) {
            isSettingPubkey.set(true)
            val nonNullProfileId = profileId ?: pubkeyProvider.getPubkey()
            val nonNullPubkey = profileIdToNostrId(nonNullProfileId)?.hex ?: nonNullProfileId
            if (profileId.isNullOrEmpty()) Log.w(TAG, "Tried to set empty pubkey for UI")

            if (nonNullPubkey == profileState.value.pubkey) {
                Log.i(TAG, "Profile of $nonNullPubkey is already set. Do nothing")
                isSettingPubkey.set(false)
            } else {
                Log.i(TAG, "Set UI for $nonNullPubkey")
                failedAppendAttempts.set(0)
                viewModelScope.launch(context = Dispatchers.IO) {
                    setProfileAndFeed(profileId = nonNullProfileId)
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
            setFeed(pubkey = profileState.value.pubkey)
            delay(1000)
            isRefreshingFlow.update { false }
        }
    }

    private val isAppending = AtomicBoolean(false)
    val onLoadMore: () -> Unit = {
        if (!isAppending.get() && failedAppendAttempts.get() <= MAX_APPEND_ATTEMPTS) {
            isAppending.set(true)
            viewModelScope.launch(context = Dispatchers.IO) {
                Log.i(TAG, "Load more")
                val pubkey = profileState.value.pubkey
                val currentFeed = feedState.value
                appendFeed(pubkey = pubkey, currentFeed = currentFeed)
                delay(APPEND_RETRY_TIME)
                if (currentFeed.lastOrNull()?.entity?.id.orEmpty() == feedState.value.lastOrNull()?.entity?.id.orEmpty()) {
                    val attempt = failedAppendAttempts.getAndIncrement()
                    Log.w(TAG, "Failed to append profile feed. Attempt $attempt")
                }
                isAppending.set(false)
            }
        }
    }

    val onCopyNprofile: () -> Unit = {
        profileState.value.nprofile.let {
            Log.i(TAG, "Copy nprofile $it")
            clip.setText(AnnotatedString(it))
            Toast.makeText(
                context,
                context.getString(R.string.profile_id_copied),
                Toast.LENGTH_SHORT
            )
                .show()
        }
    }

    private var followProcess: Job? = null
    val onFollow: (String) -> Unit = { pubkeyToFollow ->
        if (!isFollowedByMeState.value) {
            followProcess?.cancel(CancellationException("Cancelled to start follow process"))
            isFollowedByMeFlow.update { true }
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
            isFollowedByMeFlow.update { false }
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
        setFeed(pubkey = pubkey)
    }

    private fun setRecommendedRelays(recommended: List<String>) {
        recommendedRelays.clear()
        recommendedRelays.addAll(recommended)
    }

    private suspend fun setProfile(profileId: String, pubkey: String) {
        Log.i(TAG, "Set profile of $profileId")
        updateFollowFlow(newPubkey = pubkey)
        profileState = profileProvider.getProfileFlow(profileId = profileId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                if (profileState.value.pubkey == pubkey) profileState.value
                else ProfileWithMeta.createEmpty(),
            )
    }

    private suspend fun updateFollowFlow(newPubkey: String) {
        isFollowedByMeFlow.update {
            contactListProvider.listPersonalContactPubkeys().contains(newPubkey)
        }
    }

    private suspend fun setFeed(pubkey: String) {
        Log.i(TAG, "Set feed of $pubkey")
        feedState = feedProvider.getFeedFlow(
            feedSettings = getCurrentFeedSettings(pubkey = pubkey, relays = getRelays(pubkey)),
            limit = DB_BATCH_SIZE
        ).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
            if (profileState.value.pubkey == pubkey) feedState.value else emptyList()
        )
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
                until = last.entity.createdAt
            ).map { toAppend -> currentFeed.takeLast(MAX_FEED_LENGTH) + toAppend }
                .stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                    currentFeed,
                )
        }
        isRefreshingFlow.update { (false) }
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
                        contactListProvider = contactListProvider,
                        context = context,
                        clip = clip,
                    ) as T
                }
            }
    }
}
