package com.dluvian.nozzle.data.postPreparer

import androidx.compose.ui.text.AnnotatedString
import com.dluvian.nozzle.model.PostWithTagsAndMentions

interface IPostPreparer {
    fun getCleanPostWithTagsAndMentions(content: AnnotatedString): PostWithTagsAndMentions
}
