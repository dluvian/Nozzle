package com.dluvian.nozzle.ui.app.views.drawer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.model.Account
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.ui.app.navigation.NozzleNavActions
import com.dluvian.nozzle.ui.components.dropdown.SimpleDropdownMenuItem
import com.dluvian.nozzle.ui.components.iconButtons.toggle.ExpandToggleIconButton
import com.dluvian.nozzle.ui.components.icons.AddIcon
import com.dluvian.nozzle.ui.components.icons.CheckIcon
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun NozzleDrawerScreen(
    uiState: NozzleDrawerViewModelState,
    isDarkMode: Boolean,
    navActions: NozzleNavActions,
    onActivateAccount: (Int) -> Unit,
    onDeleteAccount: (Int) -> Unit,
    onToggleDarkMode: () -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = spacing.screenEdge),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        item {
            TopRow(
                modifier = Modifier.padding(horizontal = spacing.medium),
                activeAccount = uiState.activeAccount,
                allAccounts = uiState.allAccounts,
                onActiveProfileClick = {
                    navActions.navigateToProfile(uiState.activeAccount.pubkey)
                    closeDrawer()
                },
                onActivateAccount = { i ->
                    onActivateAccount(i)
                    navActions.navigateToFeed()
                    closeDrawer()
                },
                onAddAccount = {
                    navActions.navigateToAddAccount()
                    closeDrawer()
                },
                onDeleteAccount = onDeleteAccount,
                navigateToProfile = { pubkey ->
                    navActions.navigateToProfile(pubkey)
                    closeDrawer()
                },
            )
            Spacer(
                modifier = Modifier
                    .height(spacing.medium)
                    .padding(horizontal = spacing.screenEdge)
            )
            MainRows(
                modifier = Modifier.padding(spacing.screenEdge),
                isDarkMode = isDarkMode,
                onToggleDarkMode = onToggleDarkMode,
                navigateToFeed = navActions.navigateToFeed,
                navigateToInbox = navActions.navigateToInbox,
                navigateToLikes = navActions.navigateToLikes,
                navigateToSearch = navActions.navigateToSearch,
                navigateToRelayEditor = navActions.navigateToRelayEditor,
                navigateToKeys = navActions.navigateToKeys,
                closeDrawer = closeDrawer
            )
        }
        item { VersionText() }
    }
}

@Composable
private fun TopRow(
    activeAccount: Account,
    allAccounts: List<Account>,
    onActiveProfileClick: () -> Unit,
    onActivateAccount: (Int) -> Unit,
    onAddAccount: () -> Unit,
    onDeleteAccount: (Int) -> Unit,
    navigateToProfile: (String) -> Unit,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.tiny),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.small
    ) {
        val isExpanded = remember { mutableStateOf(false) }
        Column {
            ActiveAccount(
                account = activeAccount,
                isExpanded = isExpanded.value,
                onToggleExpand = { isExpanded.value = !isExpanded.value },
                onClick = onActiveProfileClick
            )
            AnimatedVisibility(visible = isExpanded.value) {
                AccountRows(
                    accounts = allAccounts,
                    onActivateAccount = onActivateAccount,
                    onAddAccount = onAddAccount,
                    onDeleteAccount = onDeleteAccount,
                    navigateToProfile = navigateToProfile,
                )
            }
        }
    }
}

@Composable
private fun ActiveAccount(
    account: Account,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onClick: () -> Unit,
) {
    TextButton(onClick = onClick) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PictureAndName(modifier = Modifier.weight(1f), account = account, isTop = true)
            ExpandToggleIconButton(isExpanded = isExpanded, onToggle = onToggleExpand)
        }
    }
}

@Composable
private fun AccountRows(
    accounts: List<Account>,
    onActivateAccount: (Int) -> Unit,
    onAddAccount: () -> Unit,
    onDeleteAccount: (Int) -> Unit,
    navigateToProfile: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        accounts.forEachIndexed { i, account ->
            AccountRow(
                account = account,
                onActivateAccount = { onActivateAccount(i) },
                onOpenProfile = { navigateToProfile(account.pubkey) },
                onDeleteAccount = { onDeleteAccount(i) })
        }
        AddAccountRow(onAddAccount = onAddAccount)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AccountRow(
    account: Account,
    onActivateAccount: () -> Unit,
    onOpenProfile: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    TextButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onActivateAccount,
    ) {
        val showMenu = remember { mutableStateOf(false) }
        AccountRowMenu(
            showMenu = showMenu.value,
            onOpenProfile = onOpenProfile,
            onDeleteAccount = if (account.isActive) null else onDeleteAccount,
            onDismiss = { showMenu.value = false }
        )
        Row(
            modifier = Modifier.combinedClickable(
                onClick = onActivateAccount,
                onLongClick = { showMenu.value = true }
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PictureAndName(modifier = Modifier.weight(1f), account = account, isTop = false)
            if (account.isActive) CheckIcon()
        }
    }
}

@Composable
private fun PictureAndName(account: Account, isTop: Boolean, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        ProfilePicture(
            modifier = Modifier
                .width(if (isTop) sizing.largeProfilePicture else sizing.smallProfilePicture)
                .aspectRatio(1f)
                .clip(CircleShape),
            pubkey = account.pubkey,
            trustType = Oneself
        )
        Spacer(Modifier.width(spacing.large))
        Text(
            text = account.name,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun AddAccountRow(onAddAccount: () -> Unit) {
    TextButton(modifier = Modifier.fillMaxWidth(), onClick = onAddAccount) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AddIcon()
            Text(text = stringResource(id = R.string.add))
        }
    }
}

@Composable
private fun MainRows(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    navigateToFeed: () -> Unit,
    navigateToInbox: () -> Unit,
    navigateToLikes: () -> Unit,
    navigateToSearch: () -> Unit,
    navigateToRelayEditor: () -> Unit,
    navigateToKeys: () -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier) {
        DrawerRow(
            imageVector = Icons.Rounded.Newspaper,
            label = stringResource(id = R.string.feed),
            action = {
                navigateToFeed()
                closeDrawer()
            }
        )
        DrawerRow(
            imageVector = Icons.Rounded.Inbox,
            label = stringResource(id = R.string.inbox),
            action = {
                navigateToInbox()
                closeDrawer()
            }
        )
        DrawerRow(
            imageVector = Icons.Rounded.Search,
            label = stringResource(id = R.string.search),
            action = {
                navigateToSearch()
                closeDrawer()
            }
        )
        DrawerRow(
            imageVector = Icons.Rounded.Favorite,
            label = stringResource(id = R.string.likes),
            action = {
                navigateToLikes()
                closeDrawer()
            }
        )
        DrawerRow(
            imageVector = Icons.Rounded.CellTower,
            label = stringResource(id = R.string.relays),
            action = {
                navigateToRelayEditor()
                closeDrawer()
            }
        )
        DrawerRow(
            imageVector = Icons.Rounded.Key,
            label = stringResource(id = R.string.keys),
            action = {
                navigateToKeys()
                closeDrawer()
            }
        )
        DrawerRow(
            imageVector = Icons.Default.DarkMode,
            label = stringResource(id = R.string.dark_mode),
            action = onToggleDarkMode,
            trailingContent = {
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = {
                        onToggleDarkMode()
                    }
                )
            }
        )
    }
}

@Composable
private fun VersionText() {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom,
    ) {
        Text(
            text = stringResource(id = R.string.nozzle_version),
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun DrawerRow(
    imageVector: ImageVector,
    label: String,
    action: () -> Unit,
    modifier: Modifier = Modifier,
    iconModifier: Modifier = Modifier,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    trailingContent: @Composable () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.tiny),
        shape = MaterialTheme.shapes.small
    ) {
        TextButton(
            onClick = action,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Icon(
                        modifier = iconModifier,
                        imageVector = imageVector,
                        contentDescription = null,
                        tint = iconTint,
                    )
                    Spacer(Modifier.width(spacing.large))
                    Text(
                        text = label,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                trailingContent()
            }
        }
    }
}

@Composable
fun AccountRowMenu(
    showMenu: Boolean,
    onOpenProfile: () -> Unit,
    onDismiss: () -> Unit,
    onDeleteAccount: (() -> Unit)? = null,
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = onDismiss
    ) {
        SimpleDropdownMenuItem(
            text = stringResource(id = R.string.open_profile),
            onClick = {
                onDismiss()
                onOpenProfile()
            }
        )
        if (onDeleteAccount != null) {
            SimpleDropdownMenuItem(
                text = stringResource(id = R.string.delete_account),
                onClick = {
                    onDismiss()
                    onDeleteAccount()
                }
            )
        }
    }
}
