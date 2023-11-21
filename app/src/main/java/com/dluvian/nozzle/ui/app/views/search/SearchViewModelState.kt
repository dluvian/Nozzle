package com.dluvian.nozzle.ui.app.views.search

data class SearchViewModelState(
    val isLoading: Boolean = false,
    val isInvalidNip05: Boolean = false,
    val searchResults: List<SearchResult> = emptyList()
)
