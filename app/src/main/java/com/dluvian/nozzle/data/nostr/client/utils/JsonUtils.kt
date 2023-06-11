package com.dluvian.nozzle.data.nostr.client.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object JsonUtils {
    val gson: Gson by lazy { GsonBuilder().disableHtmlEscaping().create() }
}
