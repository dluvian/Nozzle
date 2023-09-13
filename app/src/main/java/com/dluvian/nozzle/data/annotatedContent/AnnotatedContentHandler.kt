package com.dluvian.nozzle.data.annotatedContent

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrUriToNostrId
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.note1ToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNevent
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNevent
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNote1
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNprofile
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpub
import com.dluvian.nozzle.data.utils.UrlUtils
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId

private const val TAG = "AnnotatedContentHandler"

class AnnotatedContentHandler : IAnnotatedContentHandler {
    private val NEVENT_TAG = "NEVENT"
    private val NOTE1_TAG = "NOTE1"
    private val NPROFILE_TAG = "NPROFILE"
    private val NPUB_TAG = "NPUB"

    private val hyperlinkStyle = SpanStyle(
        color = Color.Blue,
        textDecoration = TextDecoration.Underline
    )

    private val mentionStyle = SpanStyle(color = Color.Blue)

    private val nostrUriPattern by lazy {
        Regex(pattern = "nostr:(npub1|note1|nevent1|nprofile1)[a-zA-Z0-9]+")
    }

    @OptIn(ExperimentalTextApi::class)
    override fun annotateContent(
        content: String,
        mentionedPubkeyToName: Map<String, String>
    ): AnnotatedString {
        val urls = UrlUtils.extractUrls(content)
        val nostrUris = extractNostrUris(content)
        val tokens = (urls + nostrUris).sortedBy { it.range.first }.toList()
        if (tokens.isEmpty()) return AnnotatedString(text = content)

        val editedContent = StringBuilder(content)
        return buildAnnotatedString {
            for (token in tokens) {
                val firstIndex = editedContent.indexOf(token.value)
                if (firstIndex > 0) {
                    append(editedContent.subSequence(0, firstIndex))
                    editedContent.delete(0, firstIndex)
                }
                if (urls.contains(token)) {
                    pushUrlAnnotation(UrlAnnotation(url = token.value))
                    pushStyle(style = hyperlinkStyle)
                    append(token.value)
                    pop()
                    pop()
                    editedContent.delete(0, token.value.length)
                    continue
                }
                when (val nostrId = nostrUriToNostrId(token.value)) {
                    is NpubNostrId -> {
                        pushStringAnnotation(tag = NPUB_TAG, annotation = nostrId.npub)
                        pushStyle(style = mentionStyle)
                        val name = "@" + (mentionedPubkeyToName[nostrId.pubkeyHex]
                            ?: getShortenedNpub(nostrId.npub))
                        append(name)
                        pop()
                        pop()
                    }

                    is NprofileNostrId -> {
                        pushStringAnnotation(
                            tag = NPROFILE_TAG,
                            annotation = nostrId.nprofile
                        )
                        pushStyle(style = mentionStyle)
                        val name = "@" + (mentionedPubkeyToName[nostrId.pubkeyHex]
                            ?: getShortenedNprofile(nostrId.nprofile)
                            ?: nostrId.nprofile)
                        append(name)
                        pop()
                        pop()
                    }

                    is NoteNostrId -> {
                        pushStringAnnotation(tag = NOTE1_TAG, annotation = nostrId.note1)
                        pushStyle(style = mentionStyle)
                        val name = "nostr:" + (getShortenedNote1(nostrId.note1) ?: nostrId.note1)
                        append(name)
                        pop()
                        pop()
                    }

                    is NeventNostrId -> {
                        pushStringAnnotation(tag = NEVENT_TAG, annotation = nostrId.nevent)
                        pushStyle(style = mentionStyle)
                        val name = "nostr:" + (getShortenedNevent(nostrId.nevent) ?: nostrId.nevent)
                        append(name)
                        pop()
                        pop()
                    }

                    null -> {
                        Log.w(TAG, "Failed to identify ${token.value}")
                        append(token.value)
                    }
                }
                editedContent.delete(0, token.value.length)
            }
        }
    }

    @OptIn(ExperimentalTextApi::class)
    override fun extractMediaLinks(annotatedContent: AnnotatedString): List<String> {
        return annotatedContent.getUrlAnnotations(start = 0, end = annotatedContent.length)
            .map { it.item.url }
            .filter { url -> UrlUtils.mediaSuffixes.any { suffix -> url.endsWith(suffix) } }
    }

    override fun extractNevents(annotatedContent: AnnotatedString): List<Nevent> {
        val nevents = annotatedContent
            .getStringAnnotations(tag = NEVENT_TAG, start = 0, end = annotatedContent.length)
            .mapNotNull { readNevent(it.item) }
        val note1s = annotatedContent
            .getStringAnnotations(tag = NOTE1_TAG, start = 0, end = annotatedContent.length)
            .mapNotNull { note1ToHex(it.item) }
            .map { Nevent(eventId = it, relays = emptyList(), pubkey = null) }

        return nevents + note1s
    }

    private fun extractNostrUris(extractFrom: String) = nostrUriPattern.findAll(extractFrom)

}