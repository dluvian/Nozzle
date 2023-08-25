package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface IPostWithMetaProvider {
   suspend fun getPostsWithMetaFlow(
      postIds: Collection<String>,
      authorPubkeys: Collection<String>,
   ): Flow<List<PostWithMeta>>
}
