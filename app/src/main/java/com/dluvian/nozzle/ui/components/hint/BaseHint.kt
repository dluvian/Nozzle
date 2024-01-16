package com.dluvian.nozzle.ui.components.hint

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.dluvian.nozzle.ui.theme.HintGray
import com.dluvian.nozzle.ui.theme.SearchIcon

@Composable
fun BaseHint(text: String) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(imageVector = SearchIcon, contentDescription = text, tint = HintGray)
        Text(
            text = text,
            textAlign = TextAlign.Center,
            color = HintGray
        )
    }
}
