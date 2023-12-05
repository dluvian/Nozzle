package com.dluvian.nozzle.ui.app.views.post

import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.SimpleProfile

data class PostViewModelState(
    val relayStatuses: List<RelayActive> = emptyList(),
    val postToQuote: AnnotatedMentionedPost? = null,
    val quoteRelays: Collection<String> = emptyList(),
    val searchSuggestions: List<SimpleProfile> = emptyList(),
)
