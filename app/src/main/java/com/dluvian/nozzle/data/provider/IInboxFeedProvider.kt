package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface IInboxFeedProvider {
    suspend fun getInboxFeedFlow(
        limit: Int,
        until: Long = getCurrentTimeInSeconds(),
        waitForSubscription: Long = 0L
    ): Flow<List<PostWithMeta>>
}
