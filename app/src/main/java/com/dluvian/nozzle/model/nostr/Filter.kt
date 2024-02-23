package com.dluvian.nozzle.model.nostr

import com.dluvian.nozzle.data.utils.JsonUtils
import com.dluvian.nozzle.model.Pubkey
import com.google.gson.annotations.SerializedName

data class Filter(
    val ids: List<String>? = null,
    val authors: List<Pubkey>? = null,
    val kinds: List<Int>? = null,
    @SerializedName("#e") val e: List<String>? = null,
    @SerializedName("#p") val p: List<String>? = null,
    @SerializedName("#t") val t: List<String>? = null,
    val since: Long? = null,
    val until: Long? = null,
    val limit: Int? = null
) {
    fun toJson(): String = JsonUtils.gson.toJson(this)

    fun matches(event: Event): Boolean {
        return ((this.until ?: Long.MAX_VALUE) >= event.createdAt) &&
                (this.kinds == null || this.kinds.contains(event.kind)) &&
                (this.ids == null || this.ids.contains(event.id)) &&
                checkTags(filter = this.t, index = "t", event = event) &&
                checkTags(filter = this.p, index = "p", event = event) &&
                checkTags(filter = this.e, index = "e", event = event) &&
                (this.authors == null || this.authors.contains(event.pubkey)) &&
                ((this.since ?: 0) <= event.createdAt)
    }

    private fun checkTags(filter: List<String>?, index: String, event: Event): Boolean {
        return filter == null ||
                event.tags.filter { it.firstOrNull() == index }
                    .map { it.getOrNull(1) }
                    .any { tagValue -> filter.contains(tagValue) }
    }

    fun isSimpleNoteFilter() = ids != null
            && authors == null
            && kinds == Event.noteKinds
            && e == null
            && p == null
            && t == null
            && since == null
            && until == null
            && limit == null

    fun isSimpleProfileFilter() = ids == null
            && authors != null
            && kinds == listOf(Event.Kind.METADATA)
            && e == null
            && p == null
            && t == null
            && since == null
            && until == null
            && limit == null

    companion object {
        fun createProfileFilter(pubkeys: List<String>): Filter {
            return Filter(
                authors = pubkeys,
                kinds = listOf(Event.Kind.METADATA)
            )
        }

        fun createNoteFilter(
            ids: List<String>? = null,
            pubkeys: List<String>? = null,
            e: List<String>? = null,
            p: List<String>? = null,
            t: List<String>? = null,
            since: Long? = null,
            until: Long? = null,
            limit: Int? = null
        ): Filter {
            return Filter(
                ids = ids,
                authors = pubkeys,
                e = e,
                p = p,
                t = t,
                kinds = Event.noteKinds,
                since = since,
                until = until,
                limit = limit
            )
        }

        fun createReactionFilter(
            pubkeys: List<String>,
            e: List<String>? = null,
            until: Long? = null,
            limit: Int? = null,
        ): Filter {
            return Filter(
                authors = pubkeys,
                kinds = Event.noteKinds,
                e = e,
                until = until,
                limit = limit
            )
        }

        fun createContactListFilter(pubkeys: List<String>): Filter {
            return Filter(
                authors = pubkeys,
                kinds = listOf(Event.Kind.CONTACT_LIST),
            )
        }

        fun createNip65Filter(pubkeys: List<String>): Filter {
            return Filter(
                authors = pubkeys,
                kinds = listOf(Event.Kind.NIP65),
            )
        }
    }
}
