package com.dluvian.nozzle.data.provider

import com.dluvian.nozzle.model.FeedInfo
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.feedFilter.RelayFilter
import kotlinx.coroutines.flow.Flow

interface IPostWithMetaProvider {
    suspend fun getPostsWithMetaFlow(
        feedInfo: FeedInfo,
        relayFilter: RelayFilter
    ): Flow<List<PostWithMeta>>

    suspend fun getPersonalRepliesWithMetaFlow(currentId: NoteId): Flow<List<PostWithMeta>>
}
