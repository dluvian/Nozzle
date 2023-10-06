package com.dluvian.nozzle.ui.app.navigation

import android.util.Log
import androidx.compose.material.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.URI
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId
import com.dluvian.nozzle.ui.app.VMContainer
import com.dluvian.nozzle.ui.app.views.editProfile.EditProfileRoute
import com.dluvian.nozzle.ui.app.views.feed.FeedRoute
import com.dluvian.nozzle.ui.app.views.hashtag.HashtagRoute
import com.dluvian.nozzle.ui.app.views.keys.KeysRoute
import com.dluvian.nozzle.ui.app.views.post.PostRoute
import com.dluvian.nozzle.ui.app.views.profile.ProfileRoute
import com.dluvian.nozzle.ui.app.views.relayEditor.RelayEditorRoute
import com.dluvian.nozzle.ui.app.views.reply.ReplyRoute
import com.dluvian.nozzle.ui.app.views.search.SearchRoute
import com.dluvian.nozzle.ui.app.views.thread.ThreadRoute
import kotlinx.coroutines.launch

private const val TAG = "NozzleNavGraph"


@Composable
fun NozzleNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NozzleRoute.FEED,
    vmContainer: VMContainer,
    navActions: NozzleNavActions,
    drawerState: DrawerState,
) {
    val scope = rememberCoroutineScope()
    val onNavigateToEditProfile = remember {
        {
            run {
                vmContainer.editProfileViewModel.onResetUiState()
                navActions.navigateToEditProfile()
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NozzleRoute.FEED) {
            FeedRoute(
                feedViewModel = vmContainer.feedViewModel,
                onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                onPreparePost = vmContainer.postViewModel.onPreparePost,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onNavigateToProfile = navActions.navigateToProfile,
                onNavigateToThread = navActions.navigateToThread,
                onNavigateToReply = navActions.navigateToReply,
                onNavigateToPost = navActions.navigateToPost,
                onNavigateToQuote = navActions.navigateToQuote,
                onNavigateToId = navActions.navigateToId,
            )
        }
        composable(
            route = NozzleRoute.PROFILE + "/{profileId}",
            arguments = listOf(navArgument("profileId") { type = NavType.StringType })
        ) { backStackEntry ->
            vmContainer.profileViewModel.onSetProfileId(
                backStackEntry.arguments?.getString("profileId")
            )
            ProfileRoute(
                profileViewModel = vmContainer.profileViewModel,
                onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                onNavigateToThread = navActions.navigateToThread,
                onNavigateToReply = navActions.navigateToReply,
                onNavigateToEditProfile = onNavigateToEditProfile,
                onNavigateToQuote = navActions.navigateToQuote,
                onNavigateToId = navActions.navigateToId
            )
        }
        composable(NozzleRoute.SEARCH) {
            SearchRoute(
                searchViewModel = vmContainer.searchViewModel,
                onNavigateToId = navActions.navigateToId,
                onGoBack = navActions.popStack,
            )
        }
        composable(NozzleRoute.RELAY_EDITOR) {
            RelayEditorRoute(
                relayEditorViewModel = vmContainer.relayEditorViewModel,
                onGoBack = navActions.popStack,
            )
        }
        composable(NozzleRoute.KEYS) {
            KeysRoute(
                keysViewModel = vmContainer.keysViewModel,
                onResetDrawerUiState = vmContainer.drawerViewModel.onResetUiState,
                onResetFeedIconUiState = vmContainer.feedViewModel.onResetProfileIconUiState,
                onResetEditProfileUiState = vmContainer.editProfileViewModel.onResetUiState,
                onGoBack = navActions.popStack,
            )
        }
        composable(NozzleRoute.EDIT_PROFILE) {
            EditProfileRoute(
                editProfileViewModel = vmContainer.editProfileViewModel,
                onResetDrawerUiState = vmContainer.drawerViewModel.onResetUiState,
                onResetFeedIconUiState = vmContainer.feedViewModel.onResetProfileIconUiState,
                onGoBack = navActions.popStack,
            )
        }
        composable(
            route = "${NozzleRoute.THREAD}/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            vmContainer.threadViewModel.onOpenThread(
                backStackEntry.arguments?.getString("postId").orEmpty()
            )
            ThreadRoute(
                threadViewModel = vmContainer.threadViewModel,
                onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                onNavigateToProfile = navActions.navigateToProfile,
                onNavigateToReply = navActions.navigateToReply,
                onNavigateToQuote = navActions.navigateToQuote,
                onNavigateToId = navActions.navigateToId,
                onGoBack = navActions.popStack,
            )
        }
        composable(NozzleRoute.REPLY) {
            ReplyRoute(
                replyViewModel = vmContainer.replyViewModel,
                onGoBack = navActions.popStack,
            )
        }
        composable(NozzleRoute.POST) {
            PostRoute(
                postViewModel = vmContainer.postViewModel,
                onGoBack = navActions.popStack,
            )
        }
        composable(
            route = "${NozzleRoute.QUOTE}/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            val postIdToQuote = backStackEntry.arguments?.getString("postId").orEmpty()
            vmContainer.postViewModel.onPrepareQuote(postIdToQuote)
            PostRoute(
                postViewModel = vmContainer.postViewModel,
                onGoBack = navActions.popStack,
            )
        }
        composable(
            route = "${NozzleRoute.HASHTAG}/{hashtag}",
            arguments = listOf(navArgument("hashtag") { type = NavType.StringType })
        ) { backStackEntry ->
            val hashtag = backStackEntry.arguments?.getString("hashtag")
            if (hashtag != null) {
                vmContainer.hashtagViewModel.onOpenHashtag(hashtag)
                HashtagRoute(
                    hashtagViewModel = vmContainer.hashtagViewModel,
                    onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                    onNavigateToProfile = navActions.navigateToProfile,
                    onNavigateToThread = navActions.navigateToThread,
                    onNavigateToReply = navActions.navigateToReply,
                    onNavigateToQuote = navActions.navigateToQuote,
                    onNavigateToId = navActions.navigateToId,
                    onGoBack = navActions.popStack,
                )
            }
        }
        composable(
            route = NozzleRoute.ROUTER,
            deepLinks = listOf(navDeepLink { uriPattern = "$URI{nip21}" })
        ) { backStackEntry ->
            val nip21 = backStackEntry.arguments?.getString("nip21").orEmpty()
            Log.i(TAG, "Deep link $nip21")
            when (val nostrId = nostrStrToNostrId(nostrStr = nip21)) {
                is NpubNostrId, is NprofileNostrId -> navActions.navigateToProfile(nostrId.nostrStr)
                is NoteNostrId, is NeventNostrId -> navActions.navigateToThread(nostrId.nostrStr)
                null -> {
                    Log.i(TAG, "Nostr identifier $nip21 not recognized")
                    navActions.navigateToFeed()
                }
            }
        }
    }
}
