package com.dluvian.nozzle.ui.app.navigation

import android.util.Log
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.data.utils.HashtagUtils
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId
import com.dluvian.nozzle.ui.app.VMContainer

private const val TAG = "NozzleNavActions"

class NozzleNavActions(
    private val navController: NavHostController,
    private val vmContainer: VMContainer
) {
    val navigateToProfile: (String) -> Unit = { profileId ->
        if (profileId.isNotEmpty()) {
            navController.navigate("${NozzleRoute.PROFILE}/$profileId") {
                setSimpleNavOptions(optionsBuilder = this)
            }
        }
    }

    val navigateToFeed: () -> Unit = {
        navController.navigate(NozzleRoute.FEED) {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToSearch: () -> Unit = {
        navController.navigate(NozzleRoute.SEARCH) {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToRelayEditor: () -> Unit = {
        vmContainer.relayEditorViewModel.onOpenRelayEditor()
        navController.navigate(NozzleRoute.RELAY_EDITOR) {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToKeys: () -> Unit = {
        navController.navigate(NozzleRoute.KEYS) {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToEditProfile: () -> Unit = {
        navController.navigate(NozzleRoute.EDIT_PROFILE) {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToThread: (String) -> Unit = { postId ->
        navController.navigate("${NozzleRoute.THREAD}/$postId") {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToReply: () -> Unit =
        { // TODO: PostWithMeta as input and call replyViewModel.onPrepareReply
            navController.navigate(NozzleRoute.REPLY) {
                setSimpleNavOptions(optionsBuilder = this)
            }
        }

    val navigateToPost: () -> Unit = {
        navController.navigate(NozzleRoute.POST) {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToQuote: (String) -> Unit = { postId ->
        navController.navigate("${NozzleRoute.QUOTE}/${postId}") {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToId: (String) -> Unit = { id ->
        if (id.isNotEmpty()) {
            val route = if (HashtagUtils.isHashtag(id)) "${NozzleRoute.HASHTAG}/${id}"
            else when (nostrStrToNostrId(nostrStr = id)) {
                is NoteNostrId -> "${NozzleRoute.THREAD}/${id}"
                is NeventNostrId -> "${NozzleRoute.THREAD}/${id}"
                is NpubNostrId -> "${NozzleRoute.PROFILE}/${id}"
                is NprofileNostrId -> "${NozzleRoute.PROFILE}/${id}"
                null -> {
                    Log.w(TAG, "Failed to resolve $id")
                    NozzleRoute.FEED
                }
            }
            navController.navigate(route) {
                setSimpleNavOptions(optionsBuilder = this)
            }
        }
    }

    val popStack: () -> Unit = {
        navController.popBackStack()
    }

    private fun setSimpleNavOptions(optionsBuilder: NavOptionsBuilder) {
        optionsBuilder.apply {
            // Avoid multiple copies of the same destination when
            // reselecting the same item
            launchSingleTop = true
        }
    }
}
