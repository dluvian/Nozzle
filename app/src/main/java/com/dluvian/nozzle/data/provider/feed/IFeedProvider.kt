package com.dluvian.nozzle.data.provider.feed

import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.ListAndNumberFlow
import com.dluvian.nozzle.model.PostWithMeta

interface IFeedProvider {
    suspend fun getFeedFlow(
        feedSettings: FeedSettings,
        limit: Int,
        until: Long = getCurrentTimeInSeconds(),
        waitForSubscription: Long = 0L
    ): ListAndNumberFlow<PostWithMeta>
}
