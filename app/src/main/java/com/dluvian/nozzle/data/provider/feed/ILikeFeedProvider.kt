package com.dluvian.nozzle.data.provider.feed

import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface ILikeFeedProvider {
    suspend fun getLikeFeedFlow(
        limit: Int,
        until: Long = getCurrentTimeInSeconds(),
        waitForSubscription: Long = 0L
    ): Flow<List<PostWithMeta>>
}
