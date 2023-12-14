package com.dluvian.nozzle.data.utils

fun String.escapeSQLPercentChars() = this.replace("%", "\\%")