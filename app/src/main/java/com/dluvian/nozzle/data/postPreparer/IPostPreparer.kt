package com.dluvian.nozzle.data.postPreparer

import com.dluvian.nozzle.data.MAX_SUGGESTION_LENGTH
import com.dluvian.nozzle.model.PostWithTagsAndMentions
import com.dluvian.nozzle.model.SimpleProfile

interface IPostPreparer {
    fun getCleanPostWithTagsAndMentions(content: String): PostWithTagsAndMentions

    suspend fun searchProfiles(
        nameLike: String,
        limit: Int = MAX_SUGGESTION_LENGTH
    ): List<SimpleProfile>
}
