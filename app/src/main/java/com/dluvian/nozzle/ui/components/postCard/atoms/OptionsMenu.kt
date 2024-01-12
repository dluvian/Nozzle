package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.material3.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.dropdown.SimpleDropdownMenuItem

@Composable
fun OptionsMenu(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onCopyId: () -> Unit,
    onCopyContent: () -> Unit,
    onFollow: (() -> Unit)? = null,
    onUnfollow: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    DropdownMenu(
        expanded = isOpen,
        onDismissRequest = onDismiss
    ) {
        if (onFollow != null) {
            SimpleDropdownMenuItem(
                text = stringResource(id = R.string.follow),
                onClick = {
                    onFollow()
                    onDismiss()
                }
            )
        } else if (onUnfollow != null) {
            SimpleDropdownMenuItem(
                text = stringResource(id = R.string.unfollow),
                onClick = {
                    onUnfollow()
                    onDismiss()
                }
            )
        }
        SimpleDropdownMenuItem(
            text = stringResource(id = R.string.copy_id),
            onClick = {
                onCopyId()
                onDismiss()
            }
        )
        SimpleDropdownMenuItem(
            text = stringResource(id = R.string.copy_content),
            onClick = {
                onCopyContent()
                onDismiss()
            }
        )
        if (onDelete != null) {
            SimpleDropdownMenuItem(
                text = stringResource(id = R.string.attempt_deletion),
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
        }
    }
}
