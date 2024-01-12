package com.dluvian.nozzle.ui.app.views.feed

data class FeedFilterLambdas(
    val onTogglePosts: () -> Unit,
    val onToggleReplies: () -> Unit,

    val onToggleFriends: () -> Unit,
    val onToggleFriendCircle: () -> Unit,
    val onToggleGlobal: () -> Unit,

    val onToggleAutopilot: () -> Unit,
    val onToggleReadRelays: () -> Unit,
)
