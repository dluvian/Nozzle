package com.dluvian.nozzle.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.UrlUtils.removeWebsocketPrefix
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.ui.components.dropdown.CheckboxDropdownMenuItem
import com.dluvian.nozzle.ui.components.dropdown.RelaysDropdownMenu
import com.dluvian.nozzle.ui.components.iconButtons.RelayIconButton
import com.dluvian.nozzle.ui.theme.spacing


@Composable
fun SendTopBarButton(
    onSend: () -> Unit,
    onGoBack: () -> Unit,
) {
    TopBarButton(
        imageVector = Icons.AutoMirrored.Filled.Send,
        hasChanges = true, // TODO: Refactor so this won't be needed
        description = stringResource(id = R.string.send),
        onClick = onSend,
        onCanGoBack = { true },
        onGoBack = onGoBack,
    )
}


// TODO: ShowRelaysButton and ChooseRelayButton should be the same
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
    RelayIconButton(
        onClick = { showMenu.value = true },
        description = stringResource(id = R.string.show_relays)
    )
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
    RelayIconButton(
        onClick = { if (isOpenable) showMenu.value = true },
        description = stringResource(id = R.string.show_relays)
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
        border = BorderStroke(1.dp, Color.Black),
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground),
        colors =
        ButtonDefaults.outlinedButtonColors(
            contentColor = if (isFollowed) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.background,
            containerColor = if (isFollowed) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onBackground
        )

    ) {
        if (isFollowed) Text(text = stringResource(id = R.string.following))
        else Text(text = stringResource(id = R.string.follow))
    }
}
