package com.dluvian.nozzle.data.utils

import android.content.Context
import com.dluvian.nozzle.R
import com.dluvian.nozzle.data.utils.TimeConstants.DAY_IN_SECONDS
import com.dluvian.nozzle.data.utils.TimeConstants.HOUR_IN_SECONDS
import com.dluvian.nozzle.data.utils.TimeConstants.MINUTE_IN_SECONDS
import com.dluvian.nozzle.data.utils.TimeConstants.MONTH_IN_SECONDS
import com.dluvian.nozzle.data.utils.TimeConstants.WEEK_IN_SECONDS
import com.dluvian.nozzle.data.utils.TimeConstants.YEAR_IN_SECONDS

object TimeConstants {
    const val MINUTE_IN_SECONDS: Long = 60
    const val HOUR_IN_SECONDS: Long = 3_600
    const val DAY_IN_SECONDS: Long = 86_400
    const val WEEK_IN_SECONDS: Long = 604_800
    const val MONTH_IN_SECONDS: Long = 2_592_000
    const val YEAR_IN_SECONDS: Long = 31_536_000
}

private enum class TimeDimension {
    NOW, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR;

    fun getAbbreviation(context: Context): String {
        return when (this) {
            NOW -> context.getString(R.string.time_abbr_now)
            SECOND -> context.getString(R.string.time_abbr_second)
            MINUTE -> context.getString(R.string.time_abbr_minute)
            HOUR -> context.getString(R.string.time_abbr_hour)
            DAY -> context.getString(R.string.time_abbr_day)
            WEEK -> context.getString(R.string.time_abbr_week)
            MONTH -> context.getString(R.string.time_abbr_month)
            YEAR -> context.getString(R.string.time_abbr_year)
        }
    }

    fun getValueInSeconds(): Long {
        return when (this) {
            NOW -> 0
            SECOND -> 1
            MINUTE -> MINUTE_IN_SECONDS
            HOUR -> HOUR_IN_SECONDS
            DAY -> DAY_IN_SECONDS
            WEEK -> WEEK_IN_SECONDS
            MONTH -> MONTH_IN_SECONDS
            YEAR -> YEAR_IN_SECONDS
        }
    }
}

fun getCurrentTimeInSeconds(): Long = System.currentTimeMillis() / 1000

private fun getRelativeTimeDimension(differenceInSeconds: Long): TimeDimension {
    if (differenceInSeconds <= 0) return TimeDimension.NOW
    return when (differenceInSeconds) {
        in 1 until MINUTE_IN_SECONDS -> TimeDimension.SECOND
        in MINUTE_IN_SECONDS until HOUR_IN_SECONDS -> TimeDimension.MINUTE
        in HOUR_IN_SECONDS until DAY_IN_SECONDS -> TimeDimension.HOUR
        in DAY_IN_SECONDS until WEEK_IN_SECONDS -> TimeDimension.DAY
        in WEEK_IN_SECONDS until MONTH_IN_SECONDS -> TimeDimension.WEEK
        in MONTH_IN_SECONDS until YEAR_IN_SECONDS -> TimeDimension.MONTH
        else -> TimeDimension.YEAR
    }
}

fun getRelativeTimeSpanString(
    context: Context,
    from: Long,
    now: Long = getCurrentTimeInSeconds()
): String {
    val dif = now - from
    val dimension = getRelativeTimeDimension(dif)
    if (dimension == TimeDimension.NOW) return dimension.getAbbreviation(context)

    val multiplier = when (dimension) {
        TimeDimension.NOW -> 0
        else -> dif / dimension.getValueInSeconds()
    }

    return "$multiplier${dimension.getAbbreviation(context)}"
}
