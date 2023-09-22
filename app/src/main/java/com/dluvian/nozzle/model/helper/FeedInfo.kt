package com.dluvian.nozzle.model.helper

data class FeedInfo(
    val postIds: List<String>,
    val authorPubkeys: List<String>,
    val mentionedPubkeys: List<String>,
    val mentionedPostIds: List<String>,
) {
    companion object {
        fun createEmpty(): FeedInfo {
            return FeedInfo(
                postIds = emptyList(),
                authorPubkeys = emptyList(),
                mentionedPubkeys = emptyList(),
                mentionedPostIds = emptyList(),
            )
        }
    }
}
