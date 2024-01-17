package com.dluvian.nozzle.ui.components.buttons

import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun BaseButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    border: BorderStroke? = null
) {
    Button(
        modifier = modifier,
        enabled = isEnabled,
        onClick = onClick,
        colors = colors,
        border = border
    ) {
        Text(text = text)
    }
}
