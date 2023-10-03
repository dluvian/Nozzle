package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.URI
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.note1UriToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.npubUriToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.profileIdToNostrId
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNeventUri
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNprofileUri
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile
import com.dluvian.nozzle.model.nostr.Post

object MentionUtils {
    private val nostrUriPattern = Regex("nostr:(npub1|note1|nevent1|nprofile1)[a-zA-Z0-9]+")
    private val nostrUriProfilePattern = Regex("nostr:(npub1|nprofile1)[a-zA-Z0-9]+")
    private val nostrUriPostPattern = Regex("nostr:(note1|nevent1)[a-zA-Z0-9]+")
    private val mentionedProfilePattern = Regex("(nostr:|@)(npub1|nprofile1)[a-zA-Z0-9]+")
    private const val MENTION_CHAR = "@"


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

    private fun extractMentionedProfiles(extractFrom: String) =
        mentionedProfilePattern.findAll(extractFrom)

    private fun String.removeMentionCharAndNostrUri(): String {
        if (this.startsWith(MENTION_CHAR)) return this.removePrefix(MENTION_CHAR)
        if (this.startsWith(URI)) return this.removePrefix(URI)
        return this
    }

    fun getCleanPostWithMentions(content: String): Post {
        val strBuilder = StringBuilder(content.trim())
        val allMentions = extractMentionedProfiles(content)
        allMentions
            .filter { it.value.startsWith(MENTION_CHAR) }
            .filter { profileIdToNostrId(it.value.removePrefix(MENTION_CHAR)) != null }
            .sortedByDescending { it.range.first }
            .forEach {
                strBuilder.replace(
                    it.range.first,
                    it.range.last + 1,
                    URI + it.value.removePrefix(MENTION_CHAR)
                )
            }

        return Post(
            content = strBuilder.toString(),
            mentions = allMentions
                .mapNotNull { profileIdToNostrId(it.value.removeMentionCharAndNostrUri()) }
                .map { it.hex }
                .toList()
                .distinct()
        )
    }
}
