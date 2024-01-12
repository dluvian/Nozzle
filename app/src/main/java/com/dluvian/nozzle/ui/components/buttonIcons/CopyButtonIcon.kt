package com.dluvian.nozzle.ui.components.buttonIcons

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.sizing
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun CopyIcon(onCopy: () -> Unit) {
    CopyIcon(
        modifier = Modifier
            .size(sizing.smallItem)
            .clip(RoundedCornerShape(spacing.medium))
            .clickable(onClick = onCopy),
        description = stringResource(id = R.string.copy_content),
    )
}