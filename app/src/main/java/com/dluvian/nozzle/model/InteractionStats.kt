package com.dluvian.nozzle.model

data class InteractionStats(
    private val numOfRepliesPerPost: Map<String, Int>,
    private val likedByMe: List<String>,
    private val repostedByMe: List<String>,
) {
    fun getNumOfReplies(postId: String): Int {
        return numOfRepliesPerPost[postId] ?: 0
    }

    fun isLikedByMe(postId: String) = likedByMe.contains(postId)

    fun isRepostedByMe(postId: String) = repostedByMe.contains(postId)
}
