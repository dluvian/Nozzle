package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.note1UriToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.npubUriToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNeventUri
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNprofileUri
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile

object IdExtractorUtils {
    private val nostrUriProfilePattern by lazy {
        Regex(pattern = "nostr:(npub1|nprofile1)[a-zA-Z0-9]+")
    }
    private val nostrUriPostPattern by lazy {
        Regex(pattern = "nostr:(note1|nevent1)[a-zA-Z0-9]+")
    }

    fun extractNprofilesAndNpubs(contents: Collection<String>): List<Nprofile> {
        if (contents.isEmpty()) return emptyList()
        return contents.flatMap { nostrUriProfilePattern.findAll(it) }
            .mapNotNull {
                readNprofileUri(it.value)
                    ?: npubUriToHex(it.value)?.let { hex -> Nprofile(pubkey = hex) }
            }
    }

    fun extractNeventsAndNoteIds(contents: Collection<String>): List<Nevent> {
        if (contents.isEmpty()) return emptyList()
        return contents.flatMap { nostrUriPostPattern.findAll(it) }
            .mapNotNull {
                readNeventUri(it.value)
                    ?: note1UriToHex(it.value)?.let { hex -> Nevent(eventId = hex) }
            }
    }
}