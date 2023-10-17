package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.ui.components.dropdown.CheckboxDropdownMenuItem
import com.dluvian.nozzle.ui.components.dropdown.FeedSettingsDropdownMenu
import com.dluvian.nozzle.ui.components.dropdown.RelaysDropdownMenu
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun GoBackButton(onGoBack: () -> Unit) {
    Icon(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onGoBack() },
        imageVector = Icons.Default.ArrowBack,
        contentDescription = stringResource(id = R.string.return_back),
    )
}

@Composable
fun CloseButton(onGoBack: () -> Unit) {
    Icon(
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onGoBack() },
        imageVector = Icons.Default.Close,
        contentDescription = stringResource(id = R.string.close),
    )
}

@Composable
fun SendTopBarButton(
    onSend: () -> Unit,
    onGoBack: () -> Unit,
) {
    TopBarButton(
        imageVector = Icons.Default.Send,
        hasChanges = true, // TODO: Refactor so this won't be needed
        description = stringResource(id = R.string.send),
        onClick = onSend,
        onCanGoBack = { true },
        onGoBack = onGoBack,
    )
}

@Composable
fun ShowRelaysButton(relays: List<String>) {
    val showMenu = remember { mutableStateOf(false) }
    val cleanRelays = remember(relays) { relays.map { it.removeWebsocketPrefix() } }
    DropdownMenu(
        expanded = showMenu.value,
        onDismissRequest = { showMenu.value = false }
    ) {
        cleanRelays.forEach { relay ->
            CheckboxDropdownMenuItem(
                isChecked = true,
                enabled = false,
                text = relay,
                contentPadding = PaddingValues(start = spacing.medium, end = spacing.xl),
                onToggle = { },
            )
        }
    }
    RelayIcon(onClick = { showMenu.value = true })
}

@Composable
fun ChooseRelayButton(
    relays: List<RelayActive>,
    onClickIndex: (Int) -> Unit,
    isOpenable: Boolean = true,
    onRefreshOnMenuDismiss: () -> Unit = { /*Do nothing*/ },
    isAutopilot: Boolean? = null,
    autopilotEnabled: Boolean? = null,
    onToggleAutopilot: (() -> Unit)? = null
) {
    val showMenu = remember { mutableStateOf(false) }
    RelaysDropdownMenu(
        showMenu = showMenu.value,
        menuItems = relays,
        onClickIndex = onClickIndex,
        onDismiss = {
            onRefreshOnMenuDismiss()
            showMenu.value = false
        },
        isAutopilot = isAutopilot,
        autopilotEnabled = autopilotEnabled,
        onToggleAutopilot = onToggleAutopilot
    )
    RelayIcon(onClick = { if (isOpenable) showMenu.value = true })
}

@Composable
fun FeedSettingsButton(
    feedSettings: FeedSettings,
    onRefreshOnMenuDismiss: () -> Unit,
    onToggleContactsOnly: () -> Unit,
    onTogglePosts: () -> Unit,
    onToggleReplies: () -> Unit,
    isOpenable: Boolean = true,
) {
    val showMenu = remember { mutableStateOf(false) }
    FeedSettingsDropdownMenu(
        showMenu = showMenu.value,
        feedSettings = feedSettings,
        onToggleContactsOnly = onToggleContactsOnly,
        onTogglePosts = onTogglePosts,
        onToggleReplies = onToggleReplies,
        onDismiss = {
            onRefreshOnMenuDismiss()
            showMenu.value = false
        }
    )
    Icon(
        modifier = Modifier
            .clip(CircleShape)
            .size(sizing.mediumItem)
            .clickable { if (isOpenable) showMenu.value = true },
        imageVector = Icons.Default.SettingsSuggest,
        contentDescription = stringResource(id = R.string.feed_settings),
    )
}

@Composable
fun CheckTopBarButton(
    hasChanges: Boolean,
    onCheck: () -> Unit,
    onCanGoBack: (() -> Boolean)? = null,
    onGoBack: (() -> Unit)? = null,
) {
    TopBarButton(
        imageVector = Icons.Default.Check,
        hasChanges = hasChanges,
        description = stringResource(id = R.string.update),
        onClick = onCheck,
        onCanGoBack = onCanGoBack,
        onGoBack = onGoBack,
    )
}

@Composable
fun SearchTopBarButton(
    hasChanges: Boolean,
    onSearch: () -> Unit,
    onCanGoBack: (() -> Boolean)? = null,
    onGoBack: (() -> Unit)? = null,
) {
    TopBarButton(
        imageVector = Icons.Default.Search,
        hasChanges = hasChanges,
        description = stringResource(id = R.string.search),
        onClick = onSearch,
        onCanGoBack = onCanGoBack,
        onGoBack = onGoBack,
    )
}

@Composable
private fun TopBarButton(
    imageVector: ImageVector,
    hasChanges: Boolean,
    description: String,
    onClick: () -> Unit,
    onCanGoBack: (() -> Boolean)? = null,
    onGoBack: (() -> Unit)? = null,
) {
    Icon(
        modifier = Modifier
            .clip(CircleShape)
            .clickable {
                if (hasChanges) {
                    onClick()
                    if (onCanGoBack != null && onGoBack != null) {
                        if (onCanGoBack()) onGoBack()
                    }
                }
            },
        imageVector = imageVector,
        contentDescription = description,
    )
}

@Composable
fun EditProfileButton(onNavToEditProfile: () -> Unit) {
    Button(
        onClick = { onNavToEditProfile() },
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, colors.onBackground),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colors.onBackground,
            backgroundColor = colors.background
        )
    ) {
        Text(text = stringResource(id = R.string.edit))
    }
}

@Composable
fun FollowButton(
    isFollowed: Boolean,
    onFollow: () -> Unit,
    onUnfollow: () -> Unit
) {
    Button(
        onClick = if (isFollowed) onUnfollow else onFollow,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, colors.onBackground),
        colors = if (isFollowed) {
            ButtonDefaults.outlinedButtonColors(
                contentColor = colors.onBackground,
                backgroundColor = colors.background
            )
        } else {
            ButtonDefaults.outlinedButtonColors(
                contentColor = colors.background,
                backgroundColor = colors.onBackground
            )
        }
    ) {
        if (isFollowed) Text(text = stringResource(id = R.string.following))
        else Text(text = stringResource(id = R.string.follow))
    }
}
