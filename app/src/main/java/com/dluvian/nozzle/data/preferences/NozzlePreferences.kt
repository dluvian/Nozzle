package com.dluvian.nozzle.data.preferences

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf
import com.dluvian.nozzle.data.PreferenceFileNames
import com.dluvian.nozzle.model.Contacts
import com.dluvian.nozzle.model.FeedSettings
import com.dluvian.nozzle.model.UserSpecific


const val FEED_SETTINGS_IS_POSTS: String = "feed_settings_is_posts"
const val FEED_SETTINGS_IS_REPLIES: String = "feed_settings_is_replies"
const val IS_DARK_MODE: String = "is_dark_mode"

class NozzlePreferences(
    private val context: Context
) : IFeedSettingsPreferences, IDarkModePreferences {
    private val preferences = context.getSharedPreferences(
        PreferenceFileNames.NOZZLE,
        Context.MODE_PRIVATE
    )

    override val isDarkMode = mutableStateOf(isDarkMode())

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

    override fun setDarkMode(isDarkMode: Boolean) {
        this.isDarkMode.value = isDarkMode
        preferences.edit()
            .putBoolean(IS_DARK_MODE, isDarkMode)
            .apply()
    }

    private fun isDarkMode(): Boolean {
        val default = when (
            context.resources?.configuration?.uiMode?.and(Configuration.UI_MODE_NIGHT_MASK)
        ) {
            Configuration.UI_MODE_NIGHT_YES -> true
            Configuration.UI_MODE_NIGHT_NO -> false
            Configuration.UI_MODE_NIGHT_UNDEFINED -> false
            else -> false
        }
        return preferences.getBoolean(IS_DARK_MODE, default)
    }
}
