package com.dluvian.nozzle.data.provider.feed

import com.dluvian.nozzle.data.utils.getCurrentTimeInSeconds
import com.dluvian.nozzle.model.ListAndNumberFlow
import com.dluvian.nozzle.model.PostWithMeta

interface IInboxFeedProvider {
    suspend fun getInboxFeedFlow(
        relays: Collection<String>,
        limit: Int,
        until: Long = getCurrentTimeInSeconds(),
        waitForSubscription: Long = 0L
    ): ListAndNumberFlow<PostWithMeta>
}
