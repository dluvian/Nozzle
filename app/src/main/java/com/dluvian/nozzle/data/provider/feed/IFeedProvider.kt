package com.dluvian.nozzle.data.provider.feed

import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.ListAndNumberFlow
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.feedFilter.FeedFilter

interface IFeedProvider {
    suspend fun getFeedFlow(
        feedFilter: FeedFilter,
        limit: Int,
        until: Long = getCurrentTimeInSeconds(),
        waitForSubscription: Long = 0L
    ): ListAndNumberFlow<PostWithMeta>
}
