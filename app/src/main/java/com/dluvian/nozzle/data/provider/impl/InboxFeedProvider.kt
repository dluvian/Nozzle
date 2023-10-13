package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.provider.IInboxFeedProvider
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

class InboxFeedProvider : IInboxFeedProvider {
    override suspend fun getInboxFeedFlow(): Flow<List<PostWithMeta>> {
        TODO("Not yet implemented")
    }
}