package com.dluvian.nozzle.ui.app.views.post

import androidx.compose.runtime.Immutable
import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.RelayActive
import com.dluvian.nozzle.model.SimpleProfile

@Immutable
data class PostViewModelState(
    val relayStatuses: List<RelayActive> = emptyList(),
    val postToQuote: AnnotatedMentionedPost? = null,
    val quoteRelays: Collection<String> = emptyList(),
    val searchSuggestions: List<SimpleProfile> = emptyList(),
)
