package com.dluvian.nozzle.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.R


@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var hasScrolled by remember(this) { mutableStateOf(false) }
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                hasScrolled = true
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value && hasScrolled
}

@Composable
fun getYesOrNo(value: Boolean?): String? {
    return when (value) {
        null -> null
        true -> stringResource(id = R.string.yes)
        false -> stringResource(id = R.string.no)
    }
}

@Composable
fun getStrOrUnknown(value: String?): String {
    return value ?: stringResource(id = R.string.unknown)
}

@Composable
fun getStrOrUnknown(value: AnnotatedString?): AnnotatedString {
    return value ?: AnnotatedString(stringResource(id = R.string.unknown))
}
