package com.dluvian.nozzle.data.annotatedContent

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ExperimentalTextApi
import com.dluvian.nozzle.data.nostr.utils.note1UriToHex
import com.dluvian.nozzle.data.nostr.utils.readNeventUri
import com.dluvian.nozzle.data.nostr.utils.readNprofileUri
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile
import java.util.Collections

class AnnotatedContentHandler : IAnnotatedContentBuilder, IAnnotatedContentExtractor {
    val URL_TAG = "URL"
    val NEVENT_TAG = "NEVENT"
    val NOTE1_TAG = "NOTE1"
    val NPROFILE_TAG = "NPROFILE"
    val NPUB_TAG = "NPUB"

    private val whitespacePattern by lazy { Regex("\\s+") }
    private val mediaSuffixes = listOf(".jpg", ".jpeg", ".png", ".gif")

    private val cache = Collections.synchronizedMap(mutableMapOf<String, AnnotatedString>())


    // TODO: Don't annotate in PostWithMetaProvider.
    //  Do it in next level where we can start another Flow for getting names and events
    override fun annotateContent(content: String): AnnotatedString {
        val cached = cache[content]
        if (cached != null) return cached

        // TODO: also annotate URLs
        TODO()
    }

    @OptIn(ExperimentalTextApi::class)
    override fun extractMediaLinks(annotatedContent: AnnotatedString): List<String> {
        return annotatedContent.getUrlAnnotations(start = 0, end = annotatedContent.length)
            .map { it.item.url }
            .filter { url -> mediaSuffixes.any { suffix -> url.endsWith(suffix) } }
    }

    override fun extractNevents(annotatedContent: AnnotatedString): List<Nevent> {
        val nevents = annotatedContent
            .getStringAnnotations(tag = NEVENT_TAG, start = 0, end = annotatedContent.length)
            .mapNotNull { readNeventUri(it.item) }
        val note1s = annotatedContent
            .getStringAnnotations(tag = NOTE1_TAG, start = 0, end = annotatedContent.length)
            .mapNotNull { note1UriToHex(it.item) }
            .map { Nevent(eventId = it, relays = emptyList(), pubkey = null) }

        return nevents + note1s
    }

    override fun extractNprofiles(annotatedContent: AnnotatedString): List<Nprofile> {
        val nprofiles = annotatedContent
            .getStringAnnotations(tag = NPROFILE_TAG, start = 0, end = annotatedContent.length)
            .mapNotNull { readNprofileUri(it.item) }
        val npubs = annotatedContent
            .getStringAnnotations(tag = NOTE1_TAG, start = 0, end = annotatedContent.length)
            .mapNotNull { note1UriToHex(it.item) }
            .map { Nprofile(pubkey = it, relays = emptyList()) }

        return nprofiles + npubs
    }
}