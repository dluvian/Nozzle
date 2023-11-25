package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.PostThread
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface IThreadProvider {
    suspend fun getThreadFlow(
        postId: String,
        waitForSubscription: Long? = null
    ): Flow<PostThread>

    suspend fun findParents(earliestPost: PostWithMeta)
}
