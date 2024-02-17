package com.dluvian.nozzle.ui.app.navigation

import android.util.Log
import androidx.navigation.NavHostController
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.data.utils.HashtagUtils
import com.dluvian.nozzle.model.Relay
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId
import com.dluvian.nozzle.ui.app.VMContainer

private const val TAG = "NozzleNavActions"

class NozzleNavActions(
    private val navController: NavHostController,
    private val vmContainer: VMContainer,
) {
    val navigateToProfile: (String) -> Unit = { profileId ->
        if (profileId.isNotEmpty()) {
            navController.navigateToNozzleRoute("${NozzleRoute.PROFILE}/$profileId")
        }
    }

    val navigateToFollowerList: (String) -> Unit = { pubkey ->
        navController.navigateToNozzleRoute("${NozzleRoute.FOLLOWER_LIST}/${pubkey}")
    }

    val navigateToFollowedByList: (String) -> Unit = { pubkey ->
        navController.navigateToNozzleRoute("${NozzleRoute.FOLLOWED_BY_LIST}/${pubkey}")
    }

    val navigateToFeed: () -> Unit = {
        navController.navigateToNozzleRoute(NozzleRoute.FEED)
    }

    val navigateToInbox: () -> Unit = {
        vmContainer.inboxViewModel.onOpenInbox()
        navController.navigateToNozzleRoute(NozzleRoute.INBOX)
    }

    val navigateToLikes: () -> Unit = {
        vmContainer.likesViewModel.onOpenLikes()
        navController.navigateToNozzleRoute(NozzleRoute.LIKES)
    }

    val navigateToSearch: () -> Unit = {
        navController.navigateToNozzleRoute(NozzleRoute.SEARCH)
    }

    val navigateToRelayEditor: () -> Unit = {
        vmContainer.relayEditorViewModel.onOpenRelayEditor()
        navController.navigateToNozzleRoute(NozzleRoute.RELAY_EDITOR)
    }

    val navigateToRelayProfile: (Relay) -> Unit = { relay ->
        vmContainer.relayProfileViewModel.onOpenRelayProfile(relay)
        navController.navigateToNozzleRoute(NozzleRoute.RELAY_PROFILE)
    }

    val navigateToKeys: () -> Unit = {
        vmContainer.keysViewModel.onOpenKeys()
        navController.navigateToNozzleRoute(NozzleRoute.KEYS)
    }

    val navigateToSettings: () -> Unit = {
        navController.navigateToNozzleRoute(NozzleRoute.SETTINGS)
    }

    val navigateToEditProfile: () -> Unit = {
        navController.navigateToNozzleRoute(NozzleRoute.EDIT_PROFILE)
    }

    val navigateToThread: (String) -> Unit = { postId ->
        navController.navigateToNozzleRoute("${NozzleRoute.THREAD}/$postId")
    }

    val navigateToPost: () -> Unit = {
        vmContainer.postViewModel.onPreparePost()
        navController.navigateToNozzleRoute(NozzleRoute.POST)
    }

    val navigateToAddAccount: () -> Unit = {
        navController.navigateToNozzleRoute(NozzleRoute.ADD_ACCOUNT)
    }

    private val navigateToReply: () -> Unit =
        { // TODO: PostWithMeta as input and call replyViewModel.onPrepareReply
            navController.navigateToNozzleRoute(NozzleRoute.REPLY)
        }

    private val navigateToQuote: (String) -> Unit = { postId ->
        navController.navigateToNozzleRoute("${NozzleRoute.QUOTE}/${postId}")
    }

    private val navigateToId: (String) -> Unit = { id ->
        if (id.isNotEmpty()) {
            val route = if (HashtagUtils.isHashtag(id)) "${NozzleRoute.HASHTAG}/${id}"
            else when (nostrStrToNostrId(nostrStr = id)) {
                is NoteNostrId, is NeventNostrId -> "${NozzleRoute.THREAD}/${id}"
                is NpubNostrId, is NprofileNostrId -> "${NozzleRoute.PROFILE}/${id}"
                null -> {
                    Log.w(TAG, "Failed to resolve $id")
                    NozzleRoute.FEED
                }
            }
            navController.navigateToNozzleRoute(route)
        }
    }

    val popStack: () -> Unit = {
        navController.popBackStack()
    }

    private fun NavHostController.navigateToNozzleRoute(route: String) {
        this.navigate(route) {
            launchSingleTop = true
        }
    }

    fun getPostCardNavigation(): PostCardNavLambdas {
        return PostCardNavLambdas(
            onNavigateToThread = navigateToThread,
            onNavigateToProfile = navigateToProfile,
            onNavigateToReply = navigateToReply,
            onNavigateToQuote = navigateToQuote,
            onNavigateToId = navigateToId,
            onNavigateToPost = navigateToPost,
            onNavigateToRelayProfile = navigateToRelayProfile
        )
    }
}
