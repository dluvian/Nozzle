package com.dluvian.nozzle.ui.components.text

import androidx.compose.foundation.clickable
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun Username(username: String, pubkey: String, onOpenProfile: ((String) -> Unit)?) {
    Text(
        modifier = if (onOpenProfile != null) {
            Modifier.clickable { onOpenProfile(pubkey) }
        } else {
            Modifier
        },
        text = username,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
