package com.dluvian.nozzle.ui.app.navigation

import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder

class NozzleNavActions(private val navController: NavHostController) {
    val navigateToProfile: (String) -> Unit = { pubkey ->
        navController.navigate(NozzleRoute.PROFILE + "/$pubkey") {
            setSimpleNavOptions(optionsBuilder = this)
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

    val navigateToThread: (String, String?, String?) -> Unit = { postId, replyToId, replyToRootId ->
        navController.navigate(
            NozzleRoute.THREAD + "/$postId" +
                    "?replyToId=$replyToId" +
                    "?replyToRootId=$replyToRootId"
        ) {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToReply: () -> Unit = {
        navController.navigate(NozzleRoute.REPLY) {
            setSimpleNavOptions(optionsBuilder = this)
        }
    }

    val navigateToPost: () -> Unit = {
        navController.navigate(NozzleRoute.POST) {
            setSimpleNavOptions(optionsBuilder = this)
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
