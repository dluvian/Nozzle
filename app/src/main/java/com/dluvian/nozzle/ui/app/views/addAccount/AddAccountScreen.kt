package com.dluvian.nozzle.ui.app.views.addAccount

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.components.ReturnableTopBar

@Composable
fun AddAccountScreen(onGoBack: () -> Unit) {
    Column {
        ReturnableTopBar(
            text = stringResource(id = R.string.add_account),
            onGoBack = onGoBack
        )
        ScreenContent()
    }
}

@Composable
private fun ScreenContent() {
    Text(text = "LOL")
}
