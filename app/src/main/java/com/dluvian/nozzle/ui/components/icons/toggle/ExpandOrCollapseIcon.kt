package com.dluvian.nozzle.ui.components.icons.toggle

import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.icons.CollapseIcon
import com.dluvian.nozzle.ui.components.icons.ExpandIcon

@Composable
fun ExpandOrCollapseIcon(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    if (isExpanded) {
        CollapseIcon(
            modifier = modifier,
            tint = tint,
            description = stringResource(R.string.collapse)
        )
    } else {
        ExpandIcon(
            modifier = modifier,
            tint = tint,
            description = stringResource(R.string.expand)
        )
    }
}
