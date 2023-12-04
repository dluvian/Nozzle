package com.dluvian.nozzle.data.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils


fun TextFieldValue.replaceWithSuggestion(pubkey: String): TextFieldValue {
    val stringUntilCursor = this.text.take(this.selection.end)
    val stringAfterCursor = this.text.drop(this.selection.end)
    val mentionedName = stringUntilCursor.takeLastWhile { it != '@' }
    if (mentionedName.any { it.isWhitespace() }) return this
    if (!stringUntilCursor.contains("@")) return this

    var newCursorPos: Int
    val text = buildString {
        append(stringUntilCursor.removeSuffix(mentionedName))
        append(EncodingUtils.hexToNpub(pubkey))
        append(" ")
        newCursorPos = this.length
        append(stringAfterCursor)
    }

    return this.copy(
        text = text,
        selection = TextRange(newCursorPos),
        composition = null
    )
}
