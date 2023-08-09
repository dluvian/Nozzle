package com.dluvian.nozzle.data.mapper

import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface IPostMapper {
   suspend fun mapToPostsWithMetaFlow(
      postIds: Collection<String>,
      authorPubkeys: Collection<String>,
   ): Flow<List<PostWithMeta>>
}
