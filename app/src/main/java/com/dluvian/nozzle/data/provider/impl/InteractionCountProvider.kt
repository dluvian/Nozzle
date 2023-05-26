package com.dluvian.nozzle.data.provider.impl

import com.dluvian.nozzle.data.provider.IInteractionStatsProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ReactionDao
import com.dluvian.nozzle.model.InteractionStats
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
        val numOfLikesFlow = reactionDao.getNumOfLikesPerPostFlow(postIds).distinctUntilChanged()
        val numOfRepostsFlow = postDao.getNumOfRepostsPerPostFlow(postIds).distinctUntilChanged()
        val numOfRepliesFlow = postDao.getNumOfRepliesPerPostFlow(postIds).distinctUntilChanged()
        val likedByMeFlow = reactionDao.listLikedByFlow(pubkeyProvider.getPubkey(), postIds)
            .distinctUntilChanged()
        val repostedByMeFlow = postDao.listRepostedByPubkeyFlow(pubkeyProvider.getPubkey(), postIds)
            .distinctUntilChanged()

        val mainFlow = flow {
            emit(
                InteractionStats(
                    numOfLikesPerPost = mapOf(),
                    numOfRepostsPerPost = mapOf(),
                    numOfRepliesPerPost = mapOf(),
                    likedByMe = listOf(),
                    repostedByMe = listOf(),
                )
            )
        }

        return mainFlow
            .combine(numOfLikesFlow) { main, likes ->
                main.copy(numOfLikesPerPost = likes)
            }
            .combine(numOfRepostsFlow) { main, reposts ->
                main.copy(numOfRepostsPerPost = reposts)
            }
            .combine(numOfRepliesFlow) { main, replies ->
                main.copy(numOfRepliesPerPost = replies)
            }
            .combine(likedByMeFlow) { main, liked ->
                main.copy(likedByMe = liked)
            }
            .combine(repostedByMeFlow) { main, reposted ->
                main.copy(repostedByMe = reposted)
            }
    }
}
