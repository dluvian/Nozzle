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
            SimpleDropdownLine(
                text = stringResource(id = R.string.follow),
                onClick = {
                    onFollow()
                    onDismiss()
                }
            )
        } else if (onUnfollow != null) {
            SimpleDropdownLine(
                text = stringResource(id = R.string.unfollow),
                onClick = {
                    onUnfollow()
                    onDismiss()
                }
            )
        }
        SimpleDropdownLine(
            text = stringResource(id = R.string.copy_id),
            onClick = {
                onCopyId()
                onDismiss()
            }
        )
        SimpleDropdownLine(
            text = stringResource(id = R.string.copy_content),
            onClick = {
                onCopyContent()
                onDismiss()
            }
        )
        if (onDelete != null) {
            SimpleDropdownLine(
                text = stringResource(id = R.string.attempt_deletion),
                onClick = {
                    onDelete()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun SimpleDropdownLine(text: String, onClick: () -> Unit) {
    DropdownMenuItem(onClick = onClick) {
        Text(
            text = text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
