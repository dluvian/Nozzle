package com.dluvian.nozzle.model

import androidx.compose.runtime.Immutable

@Immutable
sealed class OnlineStatus

data class Online(val ping: Long) : OnlineStatus()
data object Offline : OnlineStatus()
data object Waiting : OnlineStatus()
