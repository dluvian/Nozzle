package com.dluvian.nozzle.data.nostr.nip05

import android.util.Log
import com.dluvian.nozzle.data.utils.JsonUtils
import okhttp3.OkHttpClient
import okhttp3.Request

private const val TAG = "Nip05Resolver"

class Nip05Resolver(private val httpClient: OkHttpClient) : INip05Resolver {
    private val nip05Pattern = Regex("^[A-Za-z0-9-_.]+@(.+)$")
    override suspend fun resolve(nip05: String): Nip05Result? {
        if (!isNip05(nip05)) return null

        val splitted = nip05.split("@")
        if (splitted.size != 2) return null

        return resolve(local = splitted[0], domain = splitted[1])
    }

    override fun isNip05(nip05: String): Boolean {
        return nip05Pattern.matches(nip05)
    }

    private fun resolve(local: String, domain: String): Nip05Result? {
        val response = getResponse(local = local, domain = domain) ?: return null
        val pubkey = response.names[local] ?: return null
        val relays = response.relays[pubkey].orEmpty()
        Log.i(TAG, "Resolved $local@$domain's pubkey $pubkey and relays $relays")

        return Nip05Result(pubkey = pubkey, relays = relays)
    }

    private fun getResponse(local: String, domain: String): Nip05Response? {
        val request = Request.Builder()
            .url("https://$domain/.well-known/nostr.json?name=$local")
            .build()

        return kotlin.runCatching {
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    null
                } else {
                    response.body?.string()?.let { json ->
                        JsonUtils.gson.fromJson(json, Nip05Response::class.java)
                    }
                }
            }
        }.getOrNull()
    }
}