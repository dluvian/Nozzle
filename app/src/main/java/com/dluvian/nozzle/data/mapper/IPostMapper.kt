package com.dluvian.nozzle.data.mapper

import com.dluvian.nozzle.data.room.entity.PostEntity
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface IPostMapper {
   suspend fun mapToPostsWithMetaFlow(posts: List<PostEntity>): Flow<List<PostWithMeta>>
}
