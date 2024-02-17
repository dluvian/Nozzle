package com.dluvian.nozzle.ui.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.dluvian.nozzle.AppContainer
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.data.deletor.INoteDeletor
import com.dluvian.nozzle.data.postCardInteractor.IPostCardInteractor
import com.dluvian.nozzle.data.profileFollower.IProfileFollower
import com.dluvian.nozzle.ui.app.navigation.NozzleNavActions
import com.dluvian.nozzle.ui.app.navigation.NozzleNavGraph
import com.dluvian.nozzle.ui.app.navigation.NozzleRoute
import com.dluvian.nozzle.ui.app.views.addAccount.AddAccountViewModel
import com.dluvian.nozzle.ui.app.views.drawer.NozzleDrawerRoute
import com.dluvian.nozzle.ui.app.views.drawer.NozzleDrawerViewModel
import com.dluvian.nozzle.ui.app.views.editProfile.EditProfileViewModel
import com.dluvian.nozzle.ui.app.views.feed.FeedViewModel
import com.dluvian.nozzle.ui.app.views.hashtag.HashtagViewModel
import com.dluvian.nozzle.ui.app.views.inbox.InboxViewModel
import com.dluvian.nozzle.ui.app.views.keys.KeysViewModel
import com.dluvian.nozzle.ui.app.views.likes.LikesViewModel
import com.dluvian.nozzle.ui.app.views.post.PostViewModel
import com.dluvian.nozzle.ui.app.views.profile.ProfileViewModel
import com.dluvian.nozzle.ui.app.views.profileList.ProfileListViewModel
import com.dluvian.nozzle.ui.app.views.relayEditor.RelayEditorViewModel
import com.dluvian.nozzle.ui.app.views.relayProfile.RelayProfileViewModel
import com.dluvian.nozzle.ui.app.views.reply.ReplyViewModel
import com.dluvian.nozzle.ui.app.views.search.SearchViewModel
import com.dluvian.nozzle.ui.app.views.settings.SettingsViewModel
import com.dluvian.nozzle.ui.app.views.thread.ThreadViewModel
import com.dluvian.nozzle.ui.theme.NozzleTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun NozzleApp(appContainer: AppContainer) {
    val isDarkMode by rememberSaveable(appContainer.nozzlePreferences.isDarkMode) {
        appContainer.nozzlePreferences.isDarkMode
    }
    NozzleTheme(isDarkMode = isDarkMode) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val vmContainer = VMContainer(
                drawerViewModel = viewModel(
                    factory = NozzleDrawerViewModel.provideFactory(
                        keyManager = appContainer.keyManager,
                        accountProvider = appContainer.accountProvider,
                        nozzleSubscriber = appContainer.nozzleSubscriber,
                    )
                ),
                editProfileViewModel = viewModel(
                    factory = EditProfileViewModel.provideFactory(
                        personalProfileManager = appContainer.personalProfileManager,
                    )
                ),
                profileViewModel = viewModel(
                    factory = ProfileViewModel.provideFactory(
                        feedProvider = appContainer.feedProvider,
                        relayProvider = appContainer.relayProvider,
                        profileProvider = appContainer.profileWithMetaProvider,
                        pubkeyProvider = appContainer.keyManager,
                        contactListProvider = appContainer.contactListProvider,
                    )
                ),
                profileListViewModel = viewModel(
                    factory = ProfileListViewModel.provideFactory(
                        simpleProfileProvider = appContainer.simpleProfileProvider,
                        nozzleSubscriber = appContainer.nozzleSubscriber,
                        contactDao = appContainer.roomDb.contactDao()
                    )
                ),
                keysViewModel = viewModel(
                    factory = KeysViewModel.provideFactory(
                        keyManager = appContainer.keyManager,
                    )
                ),
                feedViewModel = viewModel(
                    factory = FeedViewModel.provideFactory(
                        pubkeyProvider = appContainer.keyManager,
                        feedProvider = appContainer.feedProvider,
                        personalProfileProvider = appContainer.personalProfileManager,
                        feedSettingsPreferences = appContainer.nozzlePreferences
                    )
                ),
                inboxViewModel = viewModel(
                    factory = InboxViewModel.provideFactory(
                        inboxFeedProvider = appContainer.feedProvider,
                        relayProvider = appContainer.relayProvider,
                    )
                ),
                likesViewModel = viewModel(
                    factory = LikesViewModel.provideFactory(
                        likeFeedProvider = appContainer.feedProvider,
                        reactionDao = appContainer.roomDb.reactionDao()
                    )
                ),
                threadViewModel = viewModel(
                    factory = ThreadViewModel.provideFactory(
                        threadProvider = appContainer.threadProvider,
                    )
                ),
                replyViewModel = viewModel(
                    factory = ReplyViewModel.provideFactory(
                        nostrService = appContainer.nostrService,
                        pubkeyProvider = appContainer.keyManager,
                        personalProfileProvider = appContainer.personalProfileManager,
                        relayProvider = appContainer.relayProvider,
                        postPreparer = appContainer.postPreparer,
                        fullPostInserter = appContainer.fullPostInserter,
                        dbExcludingCache = appContainer.dbSweepExcludingCache
                    )
                ),
                postViewModel = viewModel(
                    factory = PostViewModel.provideFactory(
                        nostrService = appContainer.nostrService,
                        pubkeyProvider = appContainer.keyManager,
                        personalProfileProvider = appContainer.personalProfileManager,
                        relayProvider = appContainer.relayProvider,
                        postPreparer = appContainer.postPreparer,
                        annotatedContentHandler = appContainer.annotatedContentHandler,
                        fullPostInserter = appContainer.fullPostInserter,
                        dbExcludingCache = appContainer.dbSweepExcludingCache,
                        postDao = appContainer.roomDb.postDao(),
                    )
                ),
                searchViewModel = viewModel(
                    factory = SearchViewModel.provideFactory(
                        nip05Resolver = appContainer.nip05Resolver,
                        simpleProfileProvider = appContainer.simpleProfileProvider,
                        searchFeedProvider = appContainer.searchFeedProvider,
                        nozzleSubscriber = appContainer.nozzleSubscriber,
                    )
                ),
                hashtagViewModel = viewModel(
                    factory = HashtagViewModel.provideFactory(
                        feedProvider = appContainer.feedProvider,
                    )
                ),
                relayEditorViewModel = viewModel(
                    factory = RelayEditorViewModel.provideFactory(
                        nostrService = appContainer.nostrService,
                        relayProvider = appContainer.relayProvider,
                        pubkeyProvider = appContainer.keyManager,
                        onlineStatusProvider = appContainer.onlineStatusProvider,
                        nip65Dao = appContainer.roomDb.nip65Dao()
                    )
                ),
                relayProfileViewModel = viewModel(
                    factory = RelayProfileViewModel.provideFactory(
                        relayProfileProvider = appContainer.relayProfileProvider,
                    )
                ),
                addAccountViewModel = viewModel(
                    factory = AddAccountViewModel.provideFactory(
                        keyManager = appContainer.keyManager,
                        nozzleSubscriber = appContainer.nozzleSubscriber,
                    )
                ),
                settingsViewModel = viewModel(
                    factory = SettingsViewModel.provideFactory(
                        settingsPreferences = appContainer.nozzlePreferences
                    )
                ),
            )

            val navController = rememberNavController()
            val navActions = remember(navController) {
                NozzleNavActions(navController = navController, vmContainer = vmContainer)
            }
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()

            val hasPrivkey by rememberSaveable(appContainer.keyManager.hasPrivkey) {
                appContainer.keyManager.hasPrivkey
            }
            val showProfilePicture by appContainer.nozzlePreferences.showProfilePictures

            BackHandler(enabled = drawerState.isOpen) {
                scope.launch { drawerState.close() }
            }

            Screen(
                drawerState = drawerState,
                vmContainer = vmContainer,
                showProfilePicture = showProfilePicture,
                profileFollower = appContainer.profileFollower,
                clickedMediaUrlCache = appContainer.clickedMediaUrlCache,
                postCardInteractor = appContainer.postCardInteractor,
                noteDeletor = appContainer.noteDeletor,
                navActions = navActions,
                navController = navController,
                hasPrivkey = hasPrivkey,
                scope = scope
            )
        }
    }
}

@Composable
private fun Screen(
    drawerState: DrawerState,
    vmContainer: VMContainer,
    showProfilePicture: Boolean,
    profileFollower: IProfileFollower,
    clickedMediaUrlCache: IClickedMediaUrlCache,
    postCardInteractor: IPostCardInteractor,
    noteDeletor: INoteDeletor,
    navActions: NozzleNavActions,
    navController: NavHostController,
    hasPrivkey: Boolean,
    scope: CoroutineScope
) {
    if (hasPrivkey) {
        Drawer(
            drawerState = drawerState,
            vmContainer = vmContainer,
            showProfilePicture = showProfilePicture,
            profileFollower = profileFollower,
            clickedMediaUrlCache = clickedMediaUrlCache,
            postCardInteractor = postCardInteractor,
            noteDeletor = noteDeletor,
            navActions = navActions,
            navController = navController,
            scope = scope
        )
    } else {
        NozzleContent(
            drawerState = drawerState,
            vmContainer = vmContainer,
            showProfilePicture = showProfilePicture,
            profileFollower = profileFollower,
            clickedMediaUrlCache = clickedMediaUrlCache,
            postCardInteractor = postCardInteractor,
            noteDeletor = noteDeletor,
            navActions = navActions,
            navController = navController,
            hasPrivkey = false
        )
    }
}

@Composable
private fun Drawer(
    drawerState: DrawerState,
    vmContainer: VMContainer,
    showProfilePicture: Boolean,
    profileFollower: IProfileFollower,
    clickedMediaUrlCache: IClickedMediaUrlCache,
    postCardInteractor: IPostCardInteractor,
    noteDeletor: INoteDeletor,
    navActions: NozzleNavActions,
    navController: NavHostController,
    scope: CoroutineScope,
) {
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // TODO: Test Dismissable and PermanentDrawerSheet
            ModalDrawerSheet {
                NozzleDrawerRoute(
                    nozzleDrawerViewModel = vmContainer.drawerViewModel,
                    navActions = navActions,
                    showProfilePicture = showProfilePicture,
                    closeDrawer = { scope.launch { drawerState.close() } },
                    modifier = Modifier
                        .statusBarsPadding()
                        .navigationBarsPadding()
                )
            }
        },
    ) {
        NozzleContent(
            vmContainer = vmContainer,
            navActions = navActions,
            showProfilePicture = showProfilePicture,
            profileFollower = profileFollower,
            clickedMediaUrlCache = clickedMediaUrlCache,
            postCardInteractor = postCardInteractor,
            noteDeletor = noteDeletor,
            drawerState = drawerState,
            navController = navController,
            hasPrivkey = true
        )
    }
}

@Composable
private fun NozzleContent(
    drawerState: DrawerState,
    vmContainer: VMContainer,
    showProfilePicture: Boolean,
    profileFollower: IProfileFollower,
    clickedMediaUrlCache: IClickedMediaUrlCache,
    postCardInteractor: IPostCardInteractor,
    noteDeletor: INoteDeletor,
    navActions: NozzleNavActions,
    navController: NavHostController,
    hasPrivkey: Boolean,
) {
    Row(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        NozzleNavGraph(
            vmContainer = vmContainer,
            navActions = navActions,
            showProfilePicture = showProfilePicture,
            profileFollower = profileFollower,
            clickedMediaUrlCache = clickedMediaUrlCache,
            postCardInteractor = postCardInteractor,
            noteDeletor = noteDeletor,
            drawerState = drawerState,
            navController = navController,
            startDestination = if (hasPrivkey) NozzleRoute.FEED else NozzleRoute.ADD_ACCOUNT
        )
    }
}
