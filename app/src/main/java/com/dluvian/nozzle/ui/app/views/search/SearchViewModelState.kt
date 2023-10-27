package com.dluvian.nozzle.ui.app.views.search

data class SearchViewModelState(
    val finalId: String = "",
    val isLoading: Boolean = false,
    val isInvalidNostrId: Boolean = false,
    val isInvalidNip05: Boolean = false,
)
