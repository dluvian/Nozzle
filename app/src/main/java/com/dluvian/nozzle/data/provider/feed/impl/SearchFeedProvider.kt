package com.dluvian.nozzle.data.provider.feed.impl

import com.dluvian.nozzle.data.MAX_LIST_LENGTH
import com.dluvian.nozzle.data.provider.IPostWithMetaProvider
import com.dluvian.nozzle.data.provider.feed.ISearchFeedProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.model.FeedInfo
import com.dluvian.nozzle.model.PostWithMeta
import com.dluvian.nozzle.model.feedFilter.ReadRelays
import kotlinx.coroutines.flow.Flow

class SearchFeedProvider(
    private val postWithMetaProvider: IPostWithMetaProvider,
    private val postDao: PostDao,
) : ISearchFeedProvider {
    override suspend fun getSearchFeedFlow(searchString: String): Flow<List<PostWithMeta>> {
        val posts = postDao.getPostsWithSimilarContent(
            content = searchString,
            limit = MAX_LIST_LENGTH
        )
        val feedInfo = FeedInfo(
            postIds = posts.map { it.id },
            authorPubkeys = posts.map { it.pubkey }.distinct(),
            mentionedPubkeys = emptyList(),
            mentionedPostIds = emptyList()
        )

        return postWithMetaProvider.getPostsWithMetaFlow(
            feedInfo = feedInfo,
            relayFilter = ReadRelays
        )
    }
}
