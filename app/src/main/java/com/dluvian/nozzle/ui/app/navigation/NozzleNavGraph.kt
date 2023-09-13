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
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.note1ToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.npubToHex
import com.dluvian.nozzle.model.PostIds
import com.dluvian.nozzle.ui.app.VMContainer
import com.dluvian.nozzle.ui.app.views.editProfile.EditProfileRoute
import com.dluvian.nozzle.ui.app.views.feed.FeedRoute
import com.dluvian.nozzle.ui.app.views.keys.KeysRoute
import com.dluvian.nozzle.ui.app.views.post.PostRoute
import com.dluvian.nozzle.ui.app.views.profile.ProfileRoute
import com.dluvian.nozzle.ui.app.views.reply.ReplyRoute
import com.dluvian.nozzle.ui.app.views.search.SearchRoute
import com.dluvian.nozzle.ui.app.views.thread.ThreadRoute
import kotlinx.coroutines.launch

private const val TAG = "NozzleNavGraph"
private const val URI = "nostr:"

// TODO: Figure out transitions

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
                onNavigateToProfile = navActions.navigateToProfile,
                onNavigateToThread = navActions.navigateToThread,
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
        // TODO: Simplify. No replyToId
        composable(
            route = "${NozzleRoute.THREAD}/{postId}?replyToId={replyToId}",
            arguments = listOf(
                navArgument("postId") { type = NavType.StringType },
                navArgument("replyToId") { type = NavType.StringType },
            )
        ) { backStackEntry ->
            vmContainer.threadViewModel.onOpenThread(
                PostIds(
                    id = backStackEntry.arguments?.getString("postId").orEmpty(),
                    replyToId = backStackEntry.arguments?.getString("replyToId"),
                )
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
            route = NozzleRoute.ROUTER,
            deepLinks = listOf(navDeepLink { uriPattern = "$URI{nip21}" })
        ) { backStackEntry ->
            val nip21 = backStackEntry.arguments?.getString("nip21")
            // TODO: Add nevent and nprofile
            if (nip21?.startsWith("npub1") == true) {
                val hex = npubToHex(nip21)
                if (hex != null) navActions.navigateToProfile(hex)
                else {
                    Log.i(TAG, "npub $nip21 is invalid")
                    navActions.navigateToFeed()
                }
            } else if (nip21?.startsWith("note1") == true) {
                val hex = note1ToHex(nip21)
                if (hex != null) navActions.navigateToThread(hex, null)
                else {
                    Log.i(TAG, "note1 $nip21 is invalid")
                    navActions.navigateToFeed()
                }
            } else {
                Log.i(TAG, "Nostr identifier $nip21 not recognized")
                navActions.navigateToFeed()
            }
        }
    }
}
