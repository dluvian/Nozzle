package com.dluvian.nozzle.data.preferences

import com.dluvian.nozzle.model.FeedSettings

interface IFeedSettingsPreferences {
    fun getFeedSettings(): FeedSettings
    fun setFeedSettings(feedSettings: FeedSettings)
}
