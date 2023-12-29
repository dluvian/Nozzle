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
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.deletor.INoteDeletor
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.URI
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrStrToNostrId
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.data.subscriber.ISubscriptionQueue
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId
import com.dluvian.nozzle.ui.app.VMContainer
import com.dluvian.nozzle.ui.app.views.addAccount.AddAccountRoute
import com.dluvian.nozzle.ui.app.views.editProfile.EditProfileRoute
import com.dluvian.nozzle.ui.app.views.feed.FeedRoute
import com.dluvian.nozzle.ui.app.views.hashtag.HashtagRoute
import com.dluvian.nozzle.ui.app.views.inbox.InboxRoute
import com.dluvian.nozzle.ui.app.views.keys.KeysRoute
import com.dluvian.nozzle.ui.app.views.likes.LikesRoute
import com.dluvian.nozzle.ui.app.views.post.PostRoute
import com.dluvian.nozzle.ui.app.views.profile.ProfileRoute
import com.dluvian.nozzle.ui.app.views.profileList.ProfileListRoute
import com.dluvian.nozzle.ui.app.views.relayEditor.RelayEditorRoute
import com.dluvian.nozzle.ui.app.views.reply.ReplyRoute
import com.dluvian.nozzle.ui.app.views.search.SearchRoute
import com.dluvian.nozzle.ui.app.views.thread.ThreadRoute
import kotlinx.coroutines.launch

private const val TAG = "NozzleNavGraph"


@Composable
fun NozzleNavGraph(
    vmContainer: VMContainer,
    navActions: NozzleNavActions,
    profileFollower: IProfileFollower,
    clickedMediaUrlCache: IClickedMediaUrlCache,
    postCardInteractor: IPostCardInteractor,
    noteDeletor: INoteDeletor,
    subscriptionQueue: ISubscriptionQueue,
    drawerState: DrawerState,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = NozzleRoute.FEED,
) {
    val scope = rememberCoroutineScope()
    val postCardLambdas = remember {
        PostCardLambdas.create(
            navLambdas = navActions.getPostCardNavigation(),
            postCardInteractor = postCardInteractor,
            noteDeletor = noteDeletor,
            profileFollower = profileFollower,
            clickedMediaUrlCache = clickedMediaUrlCache,
            subscriptionQueue = subscriptionQueue
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(route = NozzleRoute.FEED) {
            FeedRoute(
                feedViewModel = vmContainer.feedViewModel,
                profileFollower = profileFollower,
                postCardLambdas = postCardLambdas,
                onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                onOpenDrawer = { scope.launch { drawerState.open() } },
                onNavigateToPost = navActions.navigateToPost,
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
                profileFollower = profileFollower,
                postCardLambdas = postCardLambdas,
                onOpenFollowerList = navActions.navigateToFollowerList,
                onOpenFollowedByList = navActions.navigateToFollowedByList,
                onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                onNavigateToEditProfile = navActions.navigateToEditProfile,
            )
        }
        composable(
            route = NozzleRoute.FOLLOWER_LIST + "/{pubkey}",
            arguments = listOf(navArgument("pubkey") { type = NavType.StringType })
        ) { backStackEntry ->
            vmContainer.profileListViewModel.onSetFollowerList(
                backStackEntry.arguments?.getString("pubkey").orEmpty()
            )
            ProfileListRoute(
                profileListViewModel = vmContainer.profileListViewModel,
                profileFollower = profileFollower,
                postCardLambdas = postCardLambdas,
                onGoBack = navActions.popStack,
            )
        }
        composable(
            route = NozzleRoute.FOLLOWED_BY_LIST + "/{pubkey}",
            arguments = listOf(navArgument("pubkey") { type = NavType.StringType })
        ) { backStackEntry ->
            vmContainer.profileListViewModel.onSetFollowedByList(
                backStackEntry.arguments?.getString("pubkey").orEmpty()
            )
            ProfileListRoute(
                profileListViewModel = vmContainer.profileListViewModel,
                profileFollower = profileFollower,
                postCardLambdas = postCardLambdas,
                onGoBack = navActions.popStack,
            )
        }
        composable(route = NozzleRoute.INBOX) {
            InboxRoute(
                inboxViewModel = vmContainer.inboxViewModel,
                profileFollower = profileFollower,
                postCardLambdas = postCardLambdas,
                onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                onGoBack = navActions.popStack,
            )
        }
        composable(route = NozzleRoute.LIKES) {
            LikesRoute(
                likesViewModel = vmContainer.likesViewModel,
                profileFollower = profileFollower,
                postCardLambdas = postCardLambdas,
                onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                onGoBack = navActions.popStack,
            )
        }
        composable(route = NozzleRoute.SEARCH) {
            SearchRoute(
                searchViewModel = vmContainer.searchViewModel,
                postCardLambdas = postCardLambdas,
                onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                onGoBack = navActions.popStack,
            )
        }
        composable(route = NozzleRoute.RELAY_EDITOR) {
            RelayEditorRoute(
                relayEditorViewModel = vmContainer.relayEditorViewModel,
                onGoBack = navActions.popStack,
            )
        }
        composable(route = NozzleRoute.KEYS) {
            KeysRoute(
                keysViewModel = vmContainer.keysViewModel,
                onGoBack = navActions.popStack,
            )
        }
        composable(route = NozzleRoute.EDIT_PROFILE) {
            EditProfileRoute(
                editProfileViewModel = vmContainer.editProfileViewModel,
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
                profileFollower = profileFollower,
                postCardLambdas = postCardLambdas,
                onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                onGoBack = navActions.popStack,
            )
        }
        composable(route = NozzleRoute.REPLY) {
            ReplyRoute(
                replyViewModel = vmContainer.replyViewModel,
                onGoBack = navActions.popStack,
            )
        }
        composable(route = NozzleRoute.POST) {
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
                    profileFollower = profileFollower,
                    postCardLambdas = postCardLambdas,
                    onPrepareReply = vmContainer.replyViewModel.onPrepareReply,
                    onGoBack = navActions.popStack,
                )
            }
        }
        composable(route = NozzleRoute.ADD_ACCOUNT) {
            vmContainer.addAccountViewModel.onReset()
            AddAccountRoute(
                addAccountViewModel = vmContainer.addAccountViewModel,
                navigateToFeed = navActions.navigateToFeed,
                onGoBack = navActions.popStack,
            )
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
