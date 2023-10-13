package com.dluvian.nozzle.data.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object JsonUtils {
    val gson: Gson = GsonBuilder().disableHtmlEscaping().create()
}
