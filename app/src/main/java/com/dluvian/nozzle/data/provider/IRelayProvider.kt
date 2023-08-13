package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.data.MAX_RELAY_REQUESTS
import com.dluvian.nozzle.model.PostWithMeta


interface IRelayProvider {
    fun getReadRelays(): List<String>

    fun getLimitedReadRelays() = getReadRelays().let {
        if (it.size > MAX_RELAY_REQUESTS) it.shuffled().take(MAX_RELAY_REQUESTS)
        else it
    }

    fun getWriteRelays(): List<String>
    fun getPostRelays(posts: List<PostWithMeta>): List<String>
    suspend fun getAutopilotRelays(): Map<String, Set<String>>
}
