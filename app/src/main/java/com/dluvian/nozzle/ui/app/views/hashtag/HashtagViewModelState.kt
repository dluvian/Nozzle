package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.compose.runtime.Immutable

@Immutable
data class HashtagViewModelState(
    val isRefreshing: Boolean = false,
    val hashtag: String = ""
)
