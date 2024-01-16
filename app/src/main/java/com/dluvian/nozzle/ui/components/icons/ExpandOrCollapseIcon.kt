package com.dluvian.nozzle.ui.components.icons

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.CollapseIcon
import com.dluvian.nozzle.ui.theme.ExpandIcon

@Composable
fun ExpandOrCollapseIcon(
    isExpanded: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
) {
    if (isExpanded) {
        Icon(
            modifier = modifier,
            imageVector = CollapseIcon,
            tint = tint,
            contentDescription = stringResource(R.string.collapse)
        )
    } else {
        Icon(
            modifier = modifier,
            imageVector = ExpandIcon,
            tint = tint,
            contentDescription = stringResource(R.string.expand)
        )
    }
}
