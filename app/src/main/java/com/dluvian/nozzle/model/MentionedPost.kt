package com.dluvian.nozzle.model

data class MentionedPost(
    val id: String,
    val pubkey: String,
    val content: String,
    val name: String,
    val picture: String,
    val createdAt: Long,
) {
    fun toPostIds(): PostIds {
        return PostIds(id = id, replyToId = null)
    }
}

data class NullableMentionedPost(
    val id: String,
    val pubkey: String,
    val content: String,
    val name: String?,
    val picture: String?,
    val createdAt: Long,
) {
    fun toMentionedPost(): MentionedPost {
        return MentionedPost(
            id = id,
            pubkey = pubkey,
            content = content,
            name = name.orEmpty(),
            picture = picture.orEmpty(),
            createdAt = createdAt,
        )
    }
}
