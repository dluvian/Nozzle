package com.dluvian.nozzle.data.preferences

import com.dluvian.nozzle.model.feedFilter.FeedFilter

interface IFeedSettingsPreferences {
    fun getFeedSettings(): FeedFilter
    fun setFeedSettings(feedFilter: FeedFilter)
}
