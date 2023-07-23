package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.provider.IInteractionStatsProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.model.InteractionStats
import com.dluvian.nozzle.model.NORMAL_DEBOUNCE
import com.dluvian.nozzle.model.firstThenDebounce
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

class InteractionStatsProvider(
    private val pubkeyProvider: IPubkeyProvider,
    private val reactionDao: ReactionDao,
    private val postDao: PostDao,
) : IInteractionStatsProvider {
    override fun getStatsFlow(postIds: List<String>): Flow<InteractionStats> {
        val numOfRepliesFlow = postDao.getNumOfRepliesPerPostFlow(postIds)
            .distinctUntilChanged()
        val likedByMeFlow = reactionDao.listLikedByFlow(pubkeyProvider.getPubkey(), postIds)
            .distinctUntilChanged()
        val repostedByMeFlow = postDao.listRepostedByPubkeyFlow(pubkeyProvider.getPubkey(), postIds)
            .distinctUntilChanged()

        val mainFlow = flow {
            emit(
                InteractionStats(
                    numOfRepliesPerPost = emptyMap(),
                    likedByMe = emptyList(),
                    repostedByMe = emptyList(),
                )
            )
        }

        return combine(
            mainFlow,
            numOfRepliesFlow.firstThenDebounce(millis = NORMAL_DEBOUNCE),
            likedByMeFlow.firstThenDebounce(millis = NORMAL_DEBOUNCE),
            repostedByMeFlow.firstThenDebounce(millis = NORMAL_DEBOUNCE)
        ) { main, numOfReplies, likedByMe, repostedByMe ->
            main.copy(
                numOfRepliesPerPost = numOfReplies,
                likedByMe = likedByMe,
                repostedByMe = repostedByMe
            )
        }
    }
}
