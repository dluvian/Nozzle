package com.dluvian.nozzle.ui.app.navigation

data class PostCardNavLambdas(
    val onNavigateToThread: (String) -> Unit,
    val onNavigateToProfile: (String) -> Unit,
    val onNavigateToReply: () -> Unit,
    val onNavigateToQuote: (String) -> Unit,
    val onNavigateToId: (String) -> Unit,
    val onNavigateToPost: () -> Unit,
)
