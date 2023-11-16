package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.FeedInfo
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface IPostWithMetaProvider {
    suspend fun getPostsWithMetaFlow(feedInfo: FeedInfo): Flow<List<PostWithMeta>>
}
