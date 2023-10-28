package com.dluvian.nozzle.ui.app.views.post

import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.RelayActive

data class PostViewModelState(
    val relayStatuses: List<RelayActive> = emptyList(),
    val postToQuote: AnnotatedMentionedPost? = null,
    val quoteRelays: Collection<String> = emptyList(),
)
