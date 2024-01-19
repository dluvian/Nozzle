package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.iconButtons.HorizShowMoreIconButton

@Composable
fun OptionsButton(
    onCopyId: () -> Unit,
    onCopyContent: () -> Unit,
    modifier: Modifier = Modifier,
    onFollow: (() -> Unit)? = null,
    onUnfollow: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    val showMenu = remember { mutableStateOf(false) }
    Box(modifier = modifier, contentAlignment = Alignment.CenterEnd) {
        OptionsMenu(
            isOpen = showMenu.value,
            onDismiss = { showMenu.value = false },
            onCopyId = onCopyId,
            onCopyContent = onCopyContent,
            onFollow = onFollow,
            onUnfollow = onUnfollow,
            onDelete = onDelete
        )
        HorizShowMoreIconButton(
            onShowMore = { showMenu.value = !showMenu.value },
            description = stringResource(id = R.string.show_options)
        )
    }
}
