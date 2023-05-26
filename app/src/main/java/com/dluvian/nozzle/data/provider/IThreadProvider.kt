package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.PostThread
import kotlinx.coroutines.flow.Flow

interface IThreadProvider {
    suspend fun getThreadFlow(
        currentPostId: String,
        replyToId: String?,
        relays: List<String>?,
        waitForSubscription: Long? = null
    ): Flow<PostThread>
}
