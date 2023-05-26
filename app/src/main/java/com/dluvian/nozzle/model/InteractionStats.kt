package com.dluvian.nozzle.model

data class InteractionStats(
    private val numOfLikesPerPost: Map<String, Int>,
    private val numOfRepostsPerPost: Map<String, Int>,
    private val numOfRepliesPerPost: Map<String, Int>,
    private val likedByMe: List<String>,
    private val repostedByMe: List<String>,
) {
    fun getNumOfLikes(postId: String): Int {
        return numOfLikesPerPost[postId] ?: 0
    }

    fun getNumOfReposts(postId: String): Int {
        return numOfRepostsPerPost[postId] ?: 0
    }

    fun getNumOfReplies(postId: String): Int {
        return numOfRepliesPerPost[postId] ?: 0
    }

    fun isLikedByMe(postId: String) = likedByMe.contains(postId)

    fun isRepostedByMe(postId: String) = repostedByMe.contains(postId)
}
