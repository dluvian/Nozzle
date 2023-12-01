package com.dluvian.nozzle.data.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun TextFieldValue.replaceWithNpub(npub: String): TextFieldValue {
    // TODO: Implement this correctly
    val newText = this.text + npub + " "
    return this.copy(text = newText, selection = TextRange(newText.length), composition = null)
}