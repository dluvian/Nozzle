package com.dluvian.nozzle.data.nostr.utils

import com.dluvian.nozzle.data.utils.decodeHex
import com.dluvian.nozzle.model.nostr.Nevent
import com.dluvian.nozzle.model.nostr.NeventNostrId
import com.dluvian.nozzle.model.nostr.NostrId
import com.dluvian.nozzle.model.nostr.NoteNostrId
import com.dluvian.nozzle.model.nostr.Nprofile
import com.dluvian.nozzle.model.nostr.NprofileNostrId
import com.dluvian.nozzle.model.nostr.NpubNostrId
import fr.acinq.secp256k1.Hex


object EncodingUtils {
    const val NPUB = "npub"
    const val NSEC = "nsec"
    const val NOTE = "note"
    const val NEVENT = "nevent"
    const val NPROFILE = "nprofile"

    const val URI = "nostr:"
    const val MENTION_CHAR = "@"

    fun hexToNpub(pubkey: String): String {
        return Bech32.encode(NPUB, pubkey.decodeHex())
    }

    fun hexToNsec(privkey: String): String {
        return Bech32.encode(NSEC, privkey.decodeHex())
    }

    fun npubToHex(npub: String): String? {
        if (!npub.startsWith(NPUB + 1)) return null
        return runCatching { Hex.encode(Bech32.decodeBytes(npub).second) }.getOrNull()
    }

    fun npubMentionToHex(npubMention: String): String? {
        return npubToHex(npub = npubMention.removeMentionCharOrNostrUri())
    }

    fun note1ToHex(note1: String): String? {
        if (!note1.startsWith(NOTE + 1)) return null
        return runCatching { Hex.encode(Bech32.decodeBytes(note1).second) }.getOrNull()
    }

    fun note1MentionToHex(note1Mention: String): String? {
        return note1ToHex(note1 = note1Mention.removeMentionCharOrNostrUri())
    }

    fun nsecToHex(nsec: String): String? {
        if (!nsec.startsWith(NSEC + 1)) return null
        return runCatching { Hex.encode(Bech32.decodeBytes(nsec).second) }.getOrNull()
    }

    fun readNevent(nevent: String): Nevent? {
        if (!nevent.startsWith(NEVENT + 1)) return null
        val tlvEntries = runCatching { Bech32.decodeBytes(nevent).second }
            .getOrNull()
            ?: return null
        val tlvResult = TLVUtils.readTLVEntries(tlvEntries)
        if (tlvResult.isFailure) return null

        return Nevent.fromTLVEntries(tlvResult.getOrNull().orEmpty())
    }

    fun readNeventMention(neventMention: String): Nevent? {
        return readNevent(nevent = neventMention.removeMentionCharOrNostrUri())
    }

    fun readNprofile(nprofile: String): Nprofile? {
        if (!nprofile.startsWith(NPROFILE + 1)) return null
        val tlvEntries = runCatching { Bech32.decodeBytes(nprofile).second }
            .getOrNull()
            ?: return null
        val tlvResult = TLVUtils.readTLVEntries(tlvEntries)
        if (tlvResult.isFailure) return null

        return Nprofile.fromTLVEntries(tlvResult.getOrNull().orEmpty())
    }

    fun readNprofileMention(nprofileMention: String): Nprofile? {
        return readNprofile(nprofile = nprofileMention.removeMentionCharOrNostrUri())
    }

    fun createNprofileStr(pubkey: String, relays: Collection<String>): String? {
        return createRelayEncodedStr(prefix = NPROFILE, id = pubkey, relays = relays)
    }

    fun createNeventStr(postId: String, relays: Collection<String>): String? {
        return createRelayEncodedStr(prefix = NEVENT, id = postId, relays = relays)
    }

    fun createNeventUri(postId: String, relays: Collection<String>): String? {
        val nevent = createNeventStr(postId = postId, relays = relays)
        return nevent?.let { URI + it }
    }

    private fun createRelayEncodedStr(
        prefix: String,
        id: String,
        relays: Collection<String>
    ): String? {
        if (prefix != NEVENT && prefix != NPROFILE || !KeyUtils.isValidPubkey(id)) return null
        val bytes = mutableListOf<ByteArray>()
        bytes.add(TLVUtils.createTLVDefaultBytes(id.decodeHex()))
        bytes.addAll(relays.map { TLVUtils.createTLVRelayBytes(it.encodeToByteArray()) })
        val allBytes = bytes.reduce { acc, newBytes -> acc + newBytes }

        return runCatching { Bech32.encode(prefix, allBytes) }.getOrNull()
    }

    fun profileIdToNostrId(profileId: String): NostrId? {
        val npubHex = npubToHex(profileId)
        if (npubHex != null) {
            return NpubNostrId(npub = profileId, pubkeyHex = npubHex)
        }
        val nprofile = readNprofile(profileId)
        if (nprofile != null) {
            return NprofileNostrId(
                nprofile = profileId,
                pubkeyHex = nprofile.pubkey,
                relays = nprofile.relays
            )
        }
        return null
    }

    fun postIdToNostrId(postId: String): NostrId? {
        val note1Hex = note1ToHex(postId)
        if (note1Hex != null) {
            return NoteNostrId(note1 = postId, noteIdHex = note1Hex)
        }
        val nevent = readNevent(postId)
        if (nevent != null) {
            return NeventNostrId(
                nevent = postId,
                noteIdHex = nevent.eventId,
                relays = nevent.relays
            )
        }
        return null
    }

    fun nostrStrToNostrId(nostrStr: String): NostrId? {
        val profileNostrId = profileIdToNostrId(nostrStr)
        if (profileNostrId != null) {
            return profileNostrId
        }
        val postNostrId = postIdToNostrId(nostrStr)
        if (postNostrId != null) {
            return postNostrId
        }
        return null
    }

    fun nostrMentionToNostrId(nostrMention: String): NostrId? {
        return nostrStrToNostrId(nostrStr = nostrMention.removeMentionCharOrNostrUri())
    }

    fun String.removeMentionCharOrNostrUri(): String {
        if (this.startsWith(MENTION_CHAR)) return this.removePrefix(MENTION_CHAR)
        if (this.startsWith(URI)) return this.removePrefix(URI)
        return this
    }
}
