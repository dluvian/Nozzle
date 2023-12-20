package com.dluvian.nozzle.model.nostr

import com.dluvian.nozzle.data.utils.JsonUtils
import com.google.gson.annotations.SerializedName

class Filter(
    val ids: List<String>? = null,
    val authors: List<String>? = null,
    val kinds: List<Int>? = null,
    @SerializedName("#e") val e: List<String>? = null,
    @SerializedName("#p") val p: List<String>? = null,
    @SerializedName("#t") val t: List<String>? = null,
    val since: Long? = null,
    val until: Long? = null,
    val limit: Int? = null
) {
    fun toJson(): String = JsonUtils.gson.toJson(this)

    companion object {
        fun createProfileFilter(pubkeys: List<String>): Filter {
            return Filter(
                authors = pubkeys,
                kinds = listOf(Event.Kind.METADATA)
            )
        }

        fun createPostFilter(
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
                kinds = listOf(Event.Kind.TEXT_NOTE, Event.Kind.REPOST),
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
                kinds = listOf(Event.Kind.REACTION, Event.Kind.REPOST),
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
