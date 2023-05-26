package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface IFeedProvider {
    suspend fun getFeedFlow(
        feedSettings: FeedSettings,
        limit: Int,
        until: Long? = null,
        waitForSubscription: Long? = null
    ): Flow<List<PostWithMeta>>
}
