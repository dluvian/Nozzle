package com.dluvian.nozzle.ui.app.views.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.colors
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CellTower
import androidx.compose.material.icons.rounded.Inbox
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Newspaper
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpub
import com.dluvian.nozzle.model.Oneself
import com.dluvian.nozzle.model.nostr.Metadata
import com.dluvian.nozzle.ui.app.navigation.NozzleNavActions
import com.dluvian.nozzle.ui.components.media.ProfilePicture
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun NozzleDrawerScreen(
    pubkeyState: DrawerViewModelState,
    metadataState: Metadata?,
    navActions: NozzleNavActions,
    closeDrawer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = spacing.screenEdge)
    ) {
        ProfileRow(
            modifier = Modifier.padding(horizontal = spacing.medium),
            picture = metadataState?.picture.orEmpty(),
            pubkey = pubkeyState.pubkey,
            npub = pubkeyState.npub,
            profileName = metadataState?.name.orEmpty(),
            navigateToProfile = navActions.navigateToProfile,
            closeDrawer = closeDrawer
        )
        Spacer(
            modifier = Modifier
                .height(spacing.medium)
                .padding(horizontal = spacing.screenEdge)
        )
        MainRows(
            modifier = Modifier.padding(spacing.screenEdge),
            navigateToFeed = navActions.navigateToFeed,
            navigateToSearch = navActions.navigateToSearch,
            navigateToRelayEditor = navActions.navigateToRelayEditor,
            navigateToInbox = navActions.navigateToInbox,
            navigateToKeys = navActions.navigateToKeys,
            closeDrawer = closeDrawer
        )
        VersionText()
    }
}

@Composable
private fun ProfileRow(
    picture: String,
    pubkey: String,
    npub: String,
    profileName: String,
    navigateToProfile: (String) -> Unit,
    closeDrawer: () -> Unit,
    modifier: Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.tiny),
        color = colors.surface,
        shape = MaterialTheme.shapes.small
    ) {
        TextButton(
            onClick = {
                navigateToProfile(pubkey)
                closeDrawer()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                ProfilePicture(
                    modifier = Modifier
                        .fillMaxWidth(0.20f)
                        .aspectRatio(1f)
                        .clip(CircleShape),
                    pictureUrl = picture,
                    pubkey = pubkey,
                    trustType = Oneself
                )
                Spacer(Modifier.width(spacing.large))
                Text(
                    text = profileName.ifEmpty { getShortenedNpub(npub) ?: npub },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.h6,
                    color = colors.onSurface
                )
            }
        }
    }
}

@Composable
private fun MainRows(
    navigateToFeed: () -> Unit,
    navigateToInbox: () -> Unit,
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
            style = MaterialTheme.typography.caption,
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
    iconTint: Color = colors.primary
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.tiny),
        color = colors.surface,
        shape = MaterialTheme.shapes.small
    ) {
        TextButton(
            onClick = action,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
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
                    style = MaterialTheme.typography.h6,
                    color = colors.onSurface
                )
            }
        }
    }
}
