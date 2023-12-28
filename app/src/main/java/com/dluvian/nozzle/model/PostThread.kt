package com.dluvian.nozzle.model

import androidx.compose.runtime.Immutable

@Immutable
data class PostThread(
    val current: PostWithMeta?,
    val previous: List<PostWithMeta>,
    val replies: List<PostWithMeta>
) {
    fun getCurrentThreadPosition(): ThreadPosition {
        return if (previous.isNotEmpty()) ThreadPosition.END else ThreadPosition.SINGLE
    }

    companion object {
        fun createEmpty(): PostThread {
            return PostThread(current = null, previous = emptyList(), replies = emptyList())
        }
    }
}
