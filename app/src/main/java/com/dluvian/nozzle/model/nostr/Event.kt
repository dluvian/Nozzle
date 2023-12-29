package com.dluvian.nozzle.model.nostr

import com.dluvian.nozzle.data.nostr.utils.KeyUtils
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.data.utils.JsonUtils.gson
import com.dluvian.nozzle.data.utils.SchnorrUtils
import com.dluvian.nozzle.data.utils.SchnorrUtils.secp256k1
import com.dluvian.nozzle.data.utils.Sha256Utils.sha256
import com.dluvian.nozzle.data.utils.UrlUtils.removeTrailingSlashes
import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName
import fr.acinq.secp256k1.Hex


typealias Tag = List<String>

fun Tag.getNip10Marker() = this.getOrNull(3)
fun Tag.getNip10RelayHint() = this.getOrNull(2)?.removeTrailingSlashes()

class Event(
    val id: String,
    val pubkey: String,
    @SerializedName("created_at") val createdAt: Long,
    val kind: Int,
    val tags: List<Tag>,
    val content: String,
    val sig: String,
) {
    object Kind {
        const val METADATA = 0
        const val TEXT_NOTE = 1
        const val CONTACT_LIST = 3
        const val DELETE = 5
        const val REPOST = 6
        const val REACTION = 7
        const val NIP65 = 10002
    }

    companion object {

        fun fromJson(json: JsonElement): Result<Event> {
            return kotlin.runCatching { gson.fromJson(json, Event::class.java) }
        }

        private fun generateId(
            pubkey: String,
            createdAt: Long,
            kind: Int,
            tags: List<Tag>,
            content: String
        ): ByteArray {
            val event = listOf(
                0,
                pubkey,
                createdAt,
                kind,
                tags,
                content
            )
            val json = gson.toJson(event)
            return sha256.digest(json.toByteArray())
        }

        fun create(kind: Int, tags: List<Tag>, content: String, keys: Keys): Event {
            val pubkey = Hex.encode(keys.pubkey)
            val createdAt = getCurrentTimeInSeconds()
            val id = generateId(
                pubkey,
                createdAt,
                kind,
                tags,
                content
            )
            val sig = SchnorrUtils.sign(id, keys.privkey)
            return Event(
                id = Hex.encode(id),
                pubkey = pubkey,
                createdAt = createdAt,
                kind = kind,
                tags = tags,
                content = content,
                sig = Hex.encode(sig)
            )
        }

        fun createMetadataEvent(metadata: Metadata, keys: Keys): Event {
            return create(
                kind = Kind.METADATA,
                tags = emptyList(),
                content = gson.toJson(metadata),
                keys = keys
            )
        }

        fun createContactListEvent(contacts: List<String>, keys: Keys): Event {
            return create(
                kind = Kind.CONTACT_LIST,
                // No relayUrl and petname. No one uses it
                tags = contacts.map { listOf("p", it) },
                content = "",
                keys = keys
            )
        }

        fun createNip65Event(nip65Relays: List<Nip65Relay>, keys: Keys): Event {
            return create(
                kind = Kind.NIP65,
                tags = nip65Relays.map {
                    val tags = mutableListOf("r", it.url)
                    if (it.isRead xor it.isWrite) {
                        tags.add(if (it.isRead) "read" else "write")
                    }
                    tags
                },
                content = "",
                keys = keys
            )
        }

        fun createTextNoteEvent(
            content: String,
            replyTo: ReplyTo?,
            mentions: List<String>,
            hashtags: List<String>,
            keys: Keys
        ): Event {
            val tags = mutableListOf<List<String>>()

            replyTo?.let { tags.add(listOf("e", it.replyTo, it.relayUrl.orEmpty(), "reply")) }

            if (mentions.isNotEmpty()) {
                tags.add(mutableListOf("p") + mentions)
            }

            if (hashtags.isNotEmpty()) {
                tags.addAll(hashtags.map { listOf("t", it) })
            }

            return create(
                kind = Kind.TEXT_NOTE,
                tags = tags,
                content = content,
                keys = keys
            )
        }

        fun createReactionEvent(
            eventId: String, // Must be last e tag
            eventPubkey: String, // Must be last p tag
            isRepost: Boolean,
            keys: Keys
        ): Event {
            val kTag = if (isRepost) Kind.REPOST else Kind.TEXT_NOTE
            return create(
                kind = Kind.REACTION,
                tags = listOf(
                    listOf("e", eventId),
                    listOf("p", eventPubkey),
                    listOf("k", "$kTag")
                ),
                content = "+",
                keys = keys
            )
        }

        fun createDeleteEvent(
            eventId: String,
            keys: Keys
        ): Event {
            return create(
                kind = Kind.DELETE,
                tags = listOf(
                    listOf("e", eventId),
                ),
                content = "",
                keys = keys
            )
        }
    }

    fun toJson(): String = gson.toJson(this)

    fun verify(): Boolean {
        val correctId = generateId(pubkey, createdAt, kind, tags, content)
        if (id != Hex.encode(correctId)) {
            return false
        }
        return secp256k1.verifySchnorr(Hex.decode(sig), Hex.decode(id), Hex.decode(pubkey))
    }

    fun getReplyId(): String? {
        val eventTags = tags.filter {
            it.size in 2..4
                    && it[0] == "e"
                    && (
                    when (it.getNip10Marker()) {
                        "reply" -> true
                        "root" -> true
                        null -> true
                        else -> false
                    }
                    )
        }
        if (eventTags.isEmpty()) return null

        val nip10Reply = eventTags.find { it.getNip10Marker() == "reply" }
        if (nip10Reply != null) return nip10Reply[1]

        val nip10Root = eventTags.find { it.getNip10Marker() == "root" }
        if (nip10Root != null) return nip10Root[1]

        // nip10 relational (deprecated)
        return when (eventTags.size) {
            1 -> eventTags[0][1]
            else -> eventTags[1][1]
        }
    }

    fun getReplyRelayHint(): String? {
        // Like getReplyId but with relay hint check
        val eventTags = tags.filter {
            it.size in 2..4
                    && it[0] == "e"
                    && it.getNip10RelayHint()?.isNotBlank() == true
                    && (
                    when (it.getNip10Marker()) {
                        "reply" -> true
                        "root" -> true
                        null -> true
                        else -> false
                    }
                    )
        }
        if (eventTags.isEmpty()) return null

        val nip10Reply = eventTags.find { it.getNip10Marker() == "reply" }
        if (nip10Reply != null) return nip10Reply.getNip10RelayHint()

        val nip10Root = eventTags.find { it.getNip10Marker() == "root" }
        if (nip10Root != null) return nip10Root.getNip10RelayHint()

        return null
    }

    private fun getFirstETag(): Tag? {
        return tags.find { it.getOrNull(0) == "e" }
    }

    fun getReactedToId(): String? {
        return getFirstETag()?.getOrNull(1)
    }

    fun getRepostedId(): String? {
        return getFirstETag()?.getOrNull(1)
    }

    fun getRepostedRelayUrlHint(): String? {
        return getFirstETag()?.getNip10RelayHint()
    }

    fun getNip65Entries(): List<Nip65Entry> {
        return tags.filter {
            it.size >= 2
                    && it.first() == "r"
                    && it[1].startsWith("wss://")
                    && it[1].length >= 10
        }
            .map {
                val restriction = it.getOrNull(2)
                Nip65Entry(
                    url = it[1].removeTrailingSlashes(),
                    isRead = restriction == null || restriction == "read",
                    isWrite = restriction == null || restriction == "write",
                )
            }
            .filter { it.url.isNotEmpty() }
            .distinctBy { it.url }
    }

    fun getContactPubkeys(): List<String> {
        return tags
            .filter { tag -> tag.size >= 2 && tag[0] == "p" }
            .map { tag -> tag[1] }
            .filter { KeyUtils.isValidHexKey(hexKey = it) }
            .toList()
    }

    fun getHashtags() = tags
        .filter { it[0] == "t" }
        .mapNotNull { it.getOrNull(1)?.trim()?.lowercase() }
        .filter { it.isNotEmpty() }
        .distinct()

    fun getMentions() = tags
        .filter { it[0] == "p" }
        .mapNotNull { it.getOrNull(1)?.trim() }
        .filter { KeyUtils.isValidHexKey(it) }
        .distinct()

    fun isLikeReaction() =
        this.kind == Kind.REACTION && (this.content.isEmpty() || this.content == "+")

    fun isPost() = this.kind == Kind.TEXT_NOTE
    fun isRepost() = this.kind == Kind.REPOST
    fun isProfileMetadata() = this.kind == Kind.METADATA
    fun isContactList() = this.kind == Kind.CONTACT_LIST
    fun isNip65() = this.kind == Kind.NIP65
}
