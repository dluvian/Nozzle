package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.note1MentionToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.npubMentionToHex
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNeventMention
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.readNprofileMention
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.Nprofile

object MentionUtils {
    private val nostrMentionPattern = Regex("(nostr:|@)(npub1|note1|nevent1|nprofile1)[a-zA-Z0-9]+")
    private val nostrMentionProfilePattern = Regex("(nostr:|@)(npub1|nprofile1)[a-zA-Z0-9]+")
    private val nostrMentionPostPattern = Regex("(nostr:|@)(note1|nevent1)[a-zA-Z0-9]+")

    fun extractNprofilesAndNpubs(contents: Collection<String>): List<Nprofile> {
        if (contents.isEmpty()) return emptyList()
        return contents.flatMap { nostrMentionProfilePattern.findAll(it) }
            .mapNotNull {
                readNprofileMention(it.value)
                    ?: npubMentionToHex(it.value)?.let { hex -> Nprofile(pubkey = hex) }
            }
    }

    fun extractNeventsAndNoteIds(contents: Collection<String>): List<Nevent> {
        if (contents.isEmpty()) return emptyList()
        return contents.flatMap { nostrMentionPostPattern.findAll(it) }
            .mapNotNull {
                readNeventMention(it.value)
                    ?: note1MentionToHex(it.value)?.let { hex -> Nevent(eventId = hex) }
            }
    }

    fun extractNostrMentions(extractFrom: String) =
        nostrMentionPattern.findAll(extractFrom).toList()
}
