package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R

@Composable
fun OptionsMenu(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCopyId: () -> Unit,
    onCopyContent: () -> Unit
) {
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(onClick = {
            onCopyId()
            onDismiss()
        }) {
            Text(
                text = stringResource(id = R.string.copy_id),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        DropdownMenuItem(onClick = {
            onCopyContent()
            onDismiss()
        }) {
            Text(
                text = stringResource(id = R.string.copy_content),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
