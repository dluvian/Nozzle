package com.dluvian.nozzle.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun HollowButton(text: String, onClick: () -> Unit, isActive: Boolean = true) {
    BaseButton(
        text = text,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isActive) MaterialTheme.colorScheme.background
            else MaterialTheme.colorScheme.onBackground,
            contentColor = if (isActive) MaterialTheme.colorScheme.onBackground
            else MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onBackground),
        onClick = onClick
    )
}
