package com.dluvian.nozzle.data.cache

import java.util.Collections

class ClickedMediaUrlCache : IClickedMediaUrlCache {
    private val cache: MutableSet<String> = Collections.synchronizedSet(mutableSetOf<String>())

    override fun insert(mediaUrl: String): Boolean {
        return cache.add(mediaUrl)
    }

    override fun contains(mediaUrl: String): Boolean {
        return cache.contains(mediaUrl)
    }
}