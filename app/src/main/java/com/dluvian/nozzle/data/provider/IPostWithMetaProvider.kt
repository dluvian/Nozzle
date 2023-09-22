package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.helper.FeedInfo
import kotlinx.coroutines.flow.Flow

interface IPostWithMetaProvider {
    suspend fun getPostsWithMetaFlow(feedInfo: FeedInfo): Flow<List<PostWithMeta>>
}
