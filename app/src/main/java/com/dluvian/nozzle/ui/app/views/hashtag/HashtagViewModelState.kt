package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.runtime.Immutable
import com.dluvian.nozzle.model.Everyone
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.MultipleRelays

@Immutable
data class HashtagViewModelState(
    val isRefreshing: Boolean = false,
    val feedSettings: FeedSettings = FeedSettings(
        isPosts = true,
        isReplies = true,
        hashtag = null,
        authorSelection = Everyone,
        relaySelection = MultipleRelays(emptyList())
    )
)
