package com.dluvian.nozzle.ui.app.views.search

import com.dluvian.nozzle.model.SimpleProfile

sealed class SearchResult

class ProfileSearchResult(val profile: SimpleProfile) : SearchResult()
