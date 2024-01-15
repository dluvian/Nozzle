package com.dluvian.nozzle.ui.components.iconButtons.toggle

import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.icons.VisibilityOffIcon
import com.dluvian.nozzle.ui.components.icons.VisibilityOnIcon

@Composable
fun VisibilityToggleIconButton(isVisible: Boolean, onToggleVisibility: () -> Unit) {
    IconButton(onClick = onToggleVisibility) {
        if (isVisible) {
            VisibilityOnIcon(description = stringResource(id = R.string.turn_visibility_off))
        } else {
            VisibilityOffIcon(description = stringResource(id = R.string.turn_visibility_on))
        }
    }
}
