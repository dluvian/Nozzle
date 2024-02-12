package com.dluvian.nozzle.ui.app.views.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.preferences.ISettingsPreferenceStates
import com.dluvian.nozzle.ui.components.scaffolds.ReturnableScaffold
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun SettingsScreen(
    preferenceStates: ISettingsPreferenceStates,
    onToggleShowProfilePictures: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onGoBack: () -> Unit,
) {
    ReturnableScaffold(
        topBarText = stringResource(id = R.string.settings),
        onGoBack = onGoBack,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.screenEdge),
        ) {
            item {
                SettingsRow(
                    header = stringResource(id = R.string.dark_mode),
                    summary = stringResource(id = R.string.changes_interface_to_darker_tones),
                    bool = preferenceStates.isDarkMode.value,
                    onClick = onToggleDarkMode
                )
            }
            item {
                SettingsRow(
                    header = stringResource(id = R.string.show_profile_pictures),
                    summary = stringResource(id = R.string.show_profile_pictures_summary),
                    bool = preferenceStates.showProfilePictures.value,
                    onClick = onToggleShowProfilePictures
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    header: String,
    summary: String,
    bool: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = spacing.large),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(weight = 1f, fill = false)) {
            Text(text = header, maxLines = 1, style = MaterialTheme.typography.titleMedium)
            Text(text = summary)
        }
        Switch(
            modifier = Modifier,
            checked = bool,
            onCheckedChange = { onClick() })
    }
}
