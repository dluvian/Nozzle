package com.dluvian.nozzle.data.utils

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils


fun TextFieldValue.replaceWithSuggestion(name: String, pubkey: String): TextFieldValue {
    val stringUntilCursor = this.text.take(this.selection.end)
    val stringAfterCursor = this.text.drop(this.selection.end)
    val mentionedName = stringUntilCursor.takeLastWhile { it != '@' }
    if (mentionedName.any { it.isWhitespace() }) return this
    if (!stringUntilCursor.contains("@")) return this

    var newCursorPos = 0
    val annotatedString = buildAnnotatedString {
        append(stringUntilCursor.removeSuffix(mentionedName))

        pushStringAnnotation(tag = "MENTION", annotation = EncodingUtils.hexToNpub(pubkey))
        append(name)
        pop()

        append(" ")
        newCursorPos = this.length
        append(stringAfterCursor.dropWhile { !it.isWhitespace() })
    }

    return this.copy(
        annotatedString = annotatedString,
        selection = TextRange(newCursorPos),
        composition = null
    )
}
