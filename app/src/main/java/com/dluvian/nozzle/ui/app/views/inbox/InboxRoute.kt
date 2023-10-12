package com.dluvian.nozzle.ui.app.views.inbox

import androidx.compose.runtime.Composable

@Composable
fun InboxRoute(
    inboxViewModel: InboxViewModel,
    onGoBack: () -> Unit,
) {
    InboxScreen()
}
