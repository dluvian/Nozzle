package com.dluvian.nozzle.ui.components.scaffolds

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.components.bars.ReturnableTopBar

@Composable
fun ReturnableScaffold(
    topBarText: String,
    onGoBack: () -> Unit,
    fab: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            ReturnableTopBar(
                text = topBarText,
                onGoBack = onGoBack,
                actions = actions
            )
        },
        floatingActionButton = fab
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(it)) {
            content()
        }
    }
}
