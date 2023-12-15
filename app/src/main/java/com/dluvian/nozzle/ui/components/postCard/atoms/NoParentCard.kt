package com.dluvian.nozzle.ui.components.postCard.atoms

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.dluvian.nozzle.R
import com.dluvian.nozzle.ui.theme.hintGray
import com.dluvian.nozzle.ui.theme.spacing

@Composable
fun PostNotFound() {
    NoParentCard(text = stringResource(id = R.string.post_not_found))
}

@Composable
fun ClickToLoadMore(onClick: () -> Unit) {
    NoParentCard(text = stringResource(id = R.string.click_to_load_more), onClick = onClick)
}

@Composable
private fun NoParentCard(text: String, onClick: (() -> Unit)? = null) {
    BorderedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.screenEdge)
            .padding(top = spacing.screenEdge)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        backgroundColor = if (onClick == null) MaterialTheme.colors.hintGray
        else MaterialTheme.colors.surface
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.screenEdge),
            text = text,
            textAlign = TextAlign.Center,
        )
    }
}
