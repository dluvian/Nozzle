package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dluvian.nozzle.ui.components.MoreIcon

@Composable
fun OptionsButton(
    onCopyId: () -> Unit,
    onCopyContent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showMenu = remember { mutableStateOf(false) }
    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        OptionsMenu(
            isOpen = showMenu.value,
            onDismiss = { showMenu.value = false },
            onCopyId = onCopyId,
            onCopyContent = onCopyContent
        )
        MoreIcon(onClick = { showMenu.value = !showMenu.value })
    }
}
