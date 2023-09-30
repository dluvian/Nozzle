package com.dluvian.nozzle.data.annotatedContent

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.UrlAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.URI
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.nostrUriToNostrId
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.note1ToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNevent
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNevent
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNote1
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNprofile
import com.dluvian.nozzle.data.nostr.utils.ShortenedNameUtils.getShortenedNpub
import com.dluvian.nozzle.data.utils.HashtagUtils
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
    private val HASHTAG = "HASHTAG"

    private val hyperlinkStyle = SpanStyle(
        color = Color.Blue,
        textDecoration = TextDecoration.Underline
    )
    private val mentionStyle = SpanStyle(color = Color.Blue)
    private val hashtagStyle = SpanStyle(color = Color.Blue)

    private val nostrUriPattern = Regex("nostr:(npub1|note1|nevent1|nprofile1)[a-zA-Z0-9]+")

    @OptIn(ExperimentalTextApi::class)
    override fun annotateContent(
        content: String,
        mentionedPubkeyToName: Map<String, String>
    ): AnnotatedString {
        val urls = UrlUtils.extractUrls(content)
        val nostrUris = extractNostrUris(content)
        val tokens = (urls + nostrUris).toMutableList()
        val hashtags = HashtagUtils.extractHashtags(content).filter { hashtag ->
            tokens.none { isOverlappingHashtag(hashtag.range, it.range) }
        }
        tokens.addAll(hashtags)

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
                } else if (hashtags.contains(token)) {
                    pushStringAnnotation(tag = HASHTAG, annotation = token.value)
                    pushStyle(style = hashtagStyle)
                    append(token.value)
                    pop()
                    pop()
                } else {
                    when (val nostrId = nostrUriToNostrId(token.value)) {
                        is NpubNostrId -> {
                            pushStringAnnotation(tag = NPUB_TAG, annotation = nostrId.npub)
                            pushStyle(style = mentionStyle)
                            val name = "@" + (mentionedPubkeyToName[nostrId.pubkeyHex]
                                ?.ifBlank { getShortenedNpub(nostrId.npub) }
                                ?: getShortenedNpub(nostrId.npub)
                                ?: nostrId.npub)
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
                                ?.ifBlank { getShortenedNprofile(nostrId.nprofile) }
                                ?: getShortenedNprofile(nostrId.nprofile)
                                ?: nostrId.nprofile)
                            append(name)
                            pop()
                            pop()
                        }

                        is NoteNostrId -> {
                            pushStringAnnotation(tag = NOTE1_TAG, annotation = nostrId.note1)
                            pushStyle(style = mentionStyle)
                            val name = URI + (getShortenedNote1(nostrId.note1) ?: nostrId.note1)
                            append(name)
                            pop()
                            pop()
                        }

                        is NeventNostrId -> {
                            pushStringAnnotation(tag = NEVENT_TAG, annotation = nostrId.nevent)
                            pushStyle(style = mentionStyle)
                            val name = URI + (getShortenedNevent(nostrId.nevent) ?: nostrId.nevent)
                            append(name)
                            pop()
                            pop()
                        }

                        null -> {
                            Log.w(TAG, "Failed to identify ${token.value}")
                            append(token.value)
                        }
                    }
                }
                editedContent.delete(0, token.value.length)
            }
            append(editedContent)
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
            .mapNotNull { readNevent(it.item)?.let { nevent -> Pair(it.start, nevent) } }
        val note1s = annotatedContent
            .getStringAnnotations(tag = NOTE1_TAG, start = 0, end = annotatedContent.length)
            .mapNotNull {
                note1ToHex(it.item)?.let { hex ->
                    Pair(
                        it.start,
                        Nevent(eventId = hex, relays = emptyList())
                    )
                }
            }

        return (nevents + note1s).sortedBy { it.first }.map { it.second }
    }

    private fun isOverlappingHashtag(hashtagRange: IntRange, otherRange: IntRange): Boolean {
        return hashtagRange.first < otherRange.first
                && hashtagRange.last > otherRange.first
    }

    private fun extractNostrUris(extractFrom: String) =
        nostrUriPattern.findAll(extractFrom).toList()
}
