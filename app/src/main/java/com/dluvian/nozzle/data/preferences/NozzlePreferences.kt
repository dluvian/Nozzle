package com.dluvian.nozzle.data.preferences

import android.content.Context
import android.util.Log
import com.dluvian.nozzle.data.PreferenceFileNames
import com.dluvian.nozzle.model.Contacts
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.UserSpecific

private const val TAG: String = "NozzlePreferences"

const val FEED_SETTINGS_IS_POSTS: String = "feed_settings_is_posts"
const val FEED_SETTINGS_IS_REPLIES: String = "feed_settings_is_replies"

class NozzlePreferences(context: Context) : IFeedSettingsPreferences {
    private val preferences =
        context.getSharedPreferences(PreferenceFileNames.NOZZLE, Context.MODE_PRIVATE)

    init {
        Log.i(TAG, "Initialize $TAG")
    }

    override fun getFeedSettings(): FeedSettings {
        val isPosts = preferences.getBoolean(FEED_SETTINGS_IS_POSTS, true)
        val isReplies = preferences.getBoolean(FEED_SETTINGS_IS_REPLIES, true)
        return FeedSettings(
            isPosts = isPosts,
            isReplies = isReplies,
            hashtag = null,
            authorSelection = Contacts,
            relaySelection = UserSpecific(emptyMap()),
        )
    }

    override fun setFeedSettings(feedSettings: FeedSettings) {
        preferences.edit()
            .putBoolean(FEED_SETTINGS_IS_POSTS, feedSettings.isPosts)
            .putBoolean(FEED_SETTINGS_IS_REPLIES, feedSettings.isReplies)
            .apply()
    }
}
