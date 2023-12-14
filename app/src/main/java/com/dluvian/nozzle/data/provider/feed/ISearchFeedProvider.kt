package com.dluvian.nozzle.data.provider.feed

import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.flow.Flow

interface ISearchFeedProvider {
    suspend fun getSearchFeedFlow(searchString: String): Flow<List<PostWithMeta>>
}
