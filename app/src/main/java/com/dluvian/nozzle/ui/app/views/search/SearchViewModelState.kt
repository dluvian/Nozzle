package com.dluvian.nozzle.ui.app.views.search

data class SearchViewModelState(
    val isLoading: Boolean = false,
    val isInvalidNip05: Boolean = false,
    val finalId: String = "",
    val searchType: SearchType = SearchType.PEOPLE,
)
