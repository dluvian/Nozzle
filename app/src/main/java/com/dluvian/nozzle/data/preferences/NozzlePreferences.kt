package com.dluvian.nozzle.data.preferences

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.mutableStateOf
import com.dluvian.nozzle.data.PreferenceFileNames
import com.dluvian.nozzle.model.feedFilter.Autopilot
import com.dluvian.nozzle.model.feedFilter.FeedFilter
import com.dluvian.nozzle.model.feedFilter.FriendCircle
import com.dluvian.nozzle.model.feedFilter.Friends
import com.dluvian.nozzle.model.feedFilter.Global
import com.dluvian.nozzle.model.feedFilter.ReadRelays

const val IS_DARK_MODE: String = "is_dark_mode"
const val FEED_SETTINGS_IS_POSTS: String = "feed_settings_is_posts"
const val FEED_SETTINGS_IS_REPLIES: String = "feed_settings_is_replies"
const val FEED_SETTINGS_PEOPLE: String = "feed_settings_people"
const val FEED_SETTINGS_RELAYS: String = "feed_settings_relays"

const val FRIENDS = "friends"
const val FRIEND_CIRCLE = "friend_circle"
const val GLOBAL = "global"
const val AUTOPILOT = "autopilot"
const val MY_READ_RELAYS = "my_read_relays"

class NozzlePreferences(
    private val context: Context
) : IDarkModePreferences, IFeedSettingsPreferences {
    private val preferences = context.getSharedPreferences(
        PreferenceFileNames.NOZZLE,
        Context.MODE_PRIVATE
    )

    override val isDarkMode = mutableStateOf(isDarkMode())

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

    override fun getFeedSettings(): FeedFilter {
        val isPosts = preferences.getBoolean(FEED_SETTINGS_IS_POSTS, true)
        val isReplies = preferences.getBoolean(FEED_SETTINGS_IS_REPLIES, true)
        val people = preferences.getString(FEED_SETTINGS_PEOPLE, FRIENDS)
        val relays = preferences.getString(FEED_SETTINGS_RELAYS, AUTOPILOT)

        return FeedFilter(
            isPosts,
            isReplies,
            hashtag = null,
            authorFilter = when (people) {
                FRIENDS -> Friends
                FRIEND_CIRCLE -> FriendCircle
                GLOBAL -> Global
                else -> Friends
            },
            relayFilter = when (relays) {
                AUTOPILOT -> Autopilot
                MY_READ_RELAYS -> ReadRelays
                else -> Autopilot
            }
        )
    }

    override fun setFeedSettings(feedFilter: FeedFilter) {
        val people = when (feedFilter.authorFilter) {
            is Friends -> FRIENDS
            is FriendCircle -> FRIEND_CIRCLE
            is Global -> GLOBAL
            else -> FRIENDS
        }
        val relays = when (feedFilter.relayFilter) {
            is Autopilot -> AUTOPILOT
            is ReadRelays -> MY_READ_RELAYS
            else -> AUTOPILOT
        }
        preferences.edit()
            .putBoolean(FEED_SETTINGS_IS_POSTS, feedFilter.isPosts)
            .putBoolean(FEED_SETTINGS_IS_REPLIES, feedFilter.isReplies)
            .putString(FEED_SETTINGS_PEOPLE, people)
            .putString(FEED_SETTINGS_RELAYS, relays)
            .apply()
    }
}
