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

    fun hexToNpub(pubkey: String): String {
        return Bech32.encode(NPUB, pubkey.decodeHex())
    }

    fun hexToNsec(privkey: String): String {
        return Bech32.encode(NSEC, privkey.decodeHex())
    }

    fun hexToNote1(postId: String): String {
        return Bech32.encode(NOTE, postId.decodeHex())
    }

    fun npubToHex(npub: String): String? {
        if (!npub.startsWith(NPUB + 1)) return null
        return runCatching { Hex.encode(Bech32.decodeBytes(npub).second) }.getOrNull()
    }

    fun npubUriToHex(npubUri: String): String? {
        return if (npubUri.startsWith(URI)) npubToHex(npubUri.drop(URI.length))
        else null
    }

    fun note1ToHex(noteId: String): String? {
        if (!noteId.startsWith(NOTE + 1)) return null
        return runCatching { Hex.encode(Bech32.decodeBytes(noteId).second) }.getOrNull()
    }

    fun note1UriToHex(note1Uri: String): String? {
        return if (note1Uri.startsWith(URI)) note1ToHex(note1Uri.drop(URI.length))
        else null
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

    fun readNeventUri(neventUri: String): Nevent? {
        return if (neventUri.startsWith(URI)) readNevent(neventUri.drop(URI.length))
        else null
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

    fun readNprofileUri(nprofileUri: String): Nprofile? {
        return if (nprofileUri.startsWith(URI)) readNprofile(nprofileUri.drop(URI.length))
        else null
    }

    fun createNprofileStr(pubkey: String, relays: Collection<String>): String? {
        if (!KeyUtils.isValidPubkey(pubkey)) return null
        val bytes = mutableListOf<ByteArray>()
        bytes.add(TLVUtils.createTLVDefaultBytes(pubkey.decodeHex()))
        bytes.addAll(relays.map { TLVUtils.createTLVRelayBytes(it.encodeToByteArray()) })
        val allBytes = bytes.reduce { acc, newBytes -> acc + newBytes }

        return runCatching { Bech32.encode(NPROFILE, allBytes) }.getOrNull()
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

    fun nostrUriToNostrId(nostrUri: String): NostrId? {
        if (!nostrUri.startsWith(URI)) return null
        return nostrStrToNostrId(nostrUri.drop(URI.length))
    }
}


