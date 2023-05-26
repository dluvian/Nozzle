package com.dluvian.nozzle.ui.components.dropdown

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun FeedSettingsDropdownMenu(
    showMenu: Boolean,
    feedSettings: FeedSettings,
    onToggleContactsOnly: () -> Unit,
    onTogglePosts: () -> Unit,
    onToggleReplies: () -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = onDismiss
    ) {
        val padding = PaddingValues(start = spacing.medium, end = spacing.xl)
        CheckboxDropdownMenuItem(
            isChecked = feedSettings.authorSelection.isContactsOnly(),
            text = stringResource(id = R.string.contacts_only),
            contentPadding = padding,
            onToggle = onToggleContactsOnly,
        )
        DropdownDivider()
        CheckboxDropdownMenuItem(
            isChecked = feedSettings.isPosts,
            text = stringResource(id = R.string.posts),
            contentPadding = padding,
            onToggle = onTogglePosts
        )
        CheckboxDropdownMenuItem(
            isChecked = feedSettings.isReplies,
            text = stringResource(id = R.string.replies),
            contentPadding = padding,
            onToggle = onToggleReplies
        )
    }
}
