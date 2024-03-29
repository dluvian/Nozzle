package com.dluvian.nozzle.ui.app.views.post

import androidx.compose.runtime.Immutable
import com.dluvian.nozzle.model.AnnotatedMentionedPost
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.relay.RelaySelection

@Immutable
data class PostViewModelState(
    val relaySelection: List<RelaySelection> = emptyList(),
    val postToQuote: AnnotatedMentionedPost? = null,
    val quoteRelays: Collection<String> = emptyList(),
    val searchSuggestions: List<SimpleProfile> = emptyList(),
)
