package com.dluvian.nozzle.data.postPreparer

import com.dluvian.nozzle.model.PostWithTagsAndMentions

interface IPostPreparer {
    fun getCleanPostWithTagsAndMentions(content: String): PostWithTagsAndMentions
}