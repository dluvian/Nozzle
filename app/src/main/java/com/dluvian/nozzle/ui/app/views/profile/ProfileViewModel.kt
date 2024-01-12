package com.dluvian.nozzle.ui.app.views.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.DB_BATCH_SIZE
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.profileIdToNostrId
import com.dluvian.nozzle.data.paginator.IPaginator
import com.dluvian.nozzle.data.paginator.Paginator
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IProfileWithMetaProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.provider.IRelayProvider
import com.dluvian.nozzle.data.provider.feed.IFeedProvider
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.data.utils.getMaxRelaysAndAddIfTooSmall
import com.dluvian.nozzle.model.CreatedAt
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.ProfileWithMeta
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.feedFilter.FeedFilter
import com.dluvian.nozzle.model.feedFilter.MultipleRelays
import com.dluvian.nozzle.model.feedFilter.SingularPerson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

private const val TAG = "ProfileViewModel"

// TODO: Sub contactlist of contacts if its your personal profile page. Only once tho

class ProfileViewModel(
    private val feedProvider: IFeedProvider,
    private val profileProvider: IProfileWithMetaProvider,
    private val relayProvider: IRelayProvider,
    private val pubkeyProvider: IPubkeyProvider,
    contactListProvider: IContactListProvider,
) : ViewModel() {
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            false
        )

    var profileState: StateFlow<ProfileWithMeta> = MutableStateFlow(ProfileWithMeta.createEmpty())
    private var profilePubkey = pubkeyProvider.getActivePubkey()

    val contactList: StateFlow<List<Pubkey>> = contactListProvider.getPersonalContactPubkeysFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(),
            contactListProvider.listPersonalContactPubkeys()
        )

    private val paginator: IPaginator<PostWithMeta, CreatedAt> = Paginator(
        scope = viewModelScope,
        onSetRefreshing = { bool -> _isRefreshing.update { bool } },
        onGetPage = { lastCreatedAt, waitForSubscription ->
            feedProvider.getFeedFlow(
                feedFilter = getFeedFilter(
                    pubkey = profilePubkey,
                    relays = getRelays(profilePubkey)
                ),
                limit = DB_BATCH_SIZE,
                until = lastCreatedAt,
                waitForSubscription = waitForSubscription
            )
        },
        onIdentifyLastParam = { post -> post?.entity?.createdAt ?: getCurrentTimeInSeconds() }
    )

    val feed = paginator.getList()
    val numOfNewPosts = paginator.getNumOfNewItems()

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
                viewModelScope.launch(context = Dispatchers.IO) {
                    setProfileAndFeed(profileId = nonNullProfileId)
                }.invokeOnCompletion {
                    isSettingPubkey.set(false)
                }
            }
        }
    }

    val onRefresh: () -> Unit = {
        paginator.refresh(waitForSubscription = true, useInitialValue = true)
    }

    val onLoadMore: () -> Unit = { paginator.loadMore() }

    private suspend fun setProfileAndFeed(profileId: String) {
        val nostrProfileId = profileIdToNostrId(profileId)
        val pubkey = nostrProfileId?.hex ?: profileId
        val isSamePubkey = pubkey == profilePubkey
        profilePubkey = pubkey
        setProfile(profileId = profileId, pubkey = pubkey)
        setRecommendedRelays(recommended = nostrProfileId?.recommendedRelays.orEmpty())
        paginator.refresh(waitForSubscription = isSamePubkey, useInitialValue = isSamePubkey)
    }

    private fun setRecommendedRelays(recommended: List<String>) {
        recommendedRelays.clear()
        recommendedRelays.addAll(recommended)
    }

    private suspend fun setProfile(profileId: String, pubkey: String) {
        Log.i(TAG, "Set profile of $profileId")
        profileState = profileProvider.getProfileFlow(profileId = profileId)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
                if (profileState.value.pubkey == pubkey) profileState.value
                else ProfileWithMeta.createEmpty(pubkey = pubkey),
            )
    }

    private fun getFeedFilter(pubkey: Pubkey, relays: List<Relay>): FeedFilter {
        return FeedFilter(
            isPosts = true,
            isReplies = true,
            hashtag = null,
            authorFilter = SingularPerson(pubkey = pubkey),
            relayFilter = MultipleRelays(relays = relays)
        )
    }

    private suspend fun getRelays(pubkey: String): List<String> {
        val relays = recommendedRelays + relayProvider.getWriteRelaysOfPubkey(pubkey)
        return getMaxRelaysAndAddIfTooSmall(from = relays, prefer = relayProvider.getReadRelays())
    }

    companion object {
        fun provideFactory(
            feedProvider: IFeedProvider,
            relayProvider: IRelayProvider,
            profileProvider: IProfileWithMetaProvider,
            pubkeyProvider: IPubkeyProvider,
            contactListProvider: IContactListProvider,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return ProfileViewModel(
                        feedProvider = feedProvider,
                        profileProvider = profileProvider,
                        relayProvider = relayProvider,
                        pubkeyProvider = pubkeyProvider,
                        contactListProvider = contactListProvider
                    ) as T
                }
            }
    }
}
