package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.provider.IInteractionStatsProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.model.InteractionStats
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class InteractionStatsProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val reactionDao: ReactionDao,
    private val postDao: PostDao,
) : IInteractionStatsProvider {
    @OptIn(FlowPreview::class)
    override fun getStatsFlow(postIds: List<String>): Flow<InteractionStats> {
        // TODO: Optimize debounce
        val numOfRepliesFlow = postDao.getNumOfRepliesPerPostFlow(postIds)
            .distinctUntilChanged()
            .debounce(300)
        val likedByMeFlow = reactionDao.listLikedByFlow(pubkeyProvider.getPubkey(), postIds)
            .distinctUntilChanged()
        val repostedByMeFlow = postDao.listRepostedByPubkeyFlow(pubkeyProvider.getPubkey(), postIds)
            .distinctUntilChanged()

        val mainFlow = flow {
            emit(
                InteractionStats(
                    numOfRepliesPerPost = mapOf(),
                    likedByMe = emptyList(),
                    repostedByMe = emptyList(),
                )
            )
        }

        return combine(
            mainFlow,
            numOfRepliesFlow,
            likedByMeFlow,
            repostedByMeFlow
        ) { main, numOfReplies, likedByMe, repostedByMe ->
            main.copy(
                numOfRepliesPerPost = numOfReplies,
                likedByMe = likedByMe,
                repostedByMe = repostedByMe
            )
        }
    }
}
