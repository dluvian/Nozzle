package com.dluvian.nozzle.ui.components.iconButtons.toggle

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.VisibilityOffIcon
import com.dluvian.nozzle.ui.theme.VisibilityOnIcon

@Composable
fun VisibilityToggleIconButton(isVisible: Boolean, onToggleVisibility: () -> Unit) {
    IconButton(onClick = onToggleVisibility) {
        if (isVisible) {
            Icon(
                imageVector = VisibilityOffIcon,
                contentDescription = stringResource(id = R.string.turn_visibility_off)
            )
        } else {
            Icon(
                imageVector = VisibilityOnIcon,
                contentDescription = stringResource(id = R.string.turn_visibility_on)
            )
        }
    }
}
