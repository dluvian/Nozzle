package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.URI
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.note1UriToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.npubUriToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNeventUri
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNprofileUri
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile

object MentionUtils {
    private val nostrUriPattern = Regex("nostr:(npub1|note1|nevent1|nprofile1)[a-zA-Z0-9]+")
    private val nostrUriProfilePattern = Regex("nostr:(npub1|nprofile1)[a-zA-Z0-9]+")
    private val nostrUriPostPattern = Regex("nostr:(note1|nevent1)[a-zA-Z0-9]+")
    private val mentionedProfilePattern = Regex("(nostr:|@)(npub1|nprofile1)[a-zA-Z0-9]+")
    const val MENTION_CHAR = "@"


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

    fun extractNostrUris(extractFrom: String) = nostrUriPattern.findAll(extractFrom).toList()

    fun extractMentionedProfiles(extractFrom: String) =
        mentionedProfilePattern.findAll(extractFrom)

    fun String.removeMentionCharAndNostrUri(): String {
        if (this.startsWith(MENTION_CHAR)) return this.removePrefix(MENTION_CHAR)
        if (this.startsWith(URI)) return this.removePrefix(URI)
        return this
    }
}
