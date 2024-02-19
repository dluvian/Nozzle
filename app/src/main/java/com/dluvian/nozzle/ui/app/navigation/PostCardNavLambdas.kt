package com.dluvian.nozzle.ui.app.navigation

import com.dluvian.nozzle.model.Relay

data class PostCardNavLambdas(
    val onNavigateToThread: (String) -> Unit,
    val onNavigateToProfile: (String) -> Unit,
    val onNavigateToReply: () -> Unit,
    val onNavigateToQuote: (String) -> Unit,
    val onNavigateToId: (String) -> Unit,
    val onNavigateToPost: () -> Unit,
    val onNavigateToRelayProfile: (Relay) -> Unit,
)
