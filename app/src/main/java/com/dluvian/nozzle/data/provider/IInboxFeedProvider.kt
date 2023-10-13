package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface IInboxFeedProvider {
    suspend fun getInboxFeedFlow(): Flow<List<PostWithMeta>>
}