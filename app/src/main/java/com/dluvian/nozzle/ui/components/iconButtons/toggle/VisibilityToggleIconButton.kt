package com.dluvian.nozzle.ui.components.iconButtons.toggle

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.VisibilityOffIcon
import com.dluvian.nozzle.ui.theme.VisibilityOnIcon
import com.dluvian.nozzle.ui.theme.sizing

@Composable
fun VisibilityToggleIconButton(isVisible: Boolean, onToggleVisibility: () -> Unit) {
    IconButton(modifier = Modifier.size(sizing.largeItem), onClick = onToggleVisibility) {
        if (isVisible) {
            Icon(
                modifier = Modifier.size(sizing.smallItem),
                imageVector = VisibilityOffIcon,
                contentDescription = stringResource(id = R.string.turn_visibility_off)
            )
        } else {
            Icon(
                modifier = Modifier.size(sizing.smallItem),
                imageVector = VisibilityOnIcon,
                contentDescription = stringResource(id = R.string.turn_visibility_on)
            )
        }
    }
}
