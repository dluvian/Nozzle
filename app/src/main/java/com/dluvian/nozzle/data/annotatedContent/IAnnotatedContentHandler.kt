package com.dluvian.nozzle.data.annotatedContent

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.nostr.Nevent

interface IAnnotatedContentHandler {
    fun annotateContent(
        content: String,
        mentionedNamesByPubkey: Map<Pubkey, String>
    ): AnnotatedString

    fun extractMediaLinks(annotatedContent: AnnotatedString): List<String>

    fun extractNevents(annotatedContent: AnnotatedString): List<Nevent>
}