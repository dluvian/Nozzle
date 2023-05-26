package com.dluvian.nozzle.model

data class PostThread(
    val current: PostWithMeta?,
    val previous: List<PostWithMeta>,
    val replies: List<PostWithMeta>
) {
    fun getList(): List<PostWithMeta> {
        val result = mutableListOf<PostWithMeta>()
        current?.let { result.add(it) }
        result.addAll(previous)
        result.addAll(replies)

        return result
    }

    fun getCurrentThreadPosition(): ThreadPosition {
        return if (previous.isNotEmpty() || current?.replyToId != null) ThreadPosition.END
        else ThreadPosition.SINGLE
    }

    companion object {
        fun createEmpty(): PostThread {
            return PostThread(current = null, previous = listOf(), replies = listOf())
        }
    }
}
