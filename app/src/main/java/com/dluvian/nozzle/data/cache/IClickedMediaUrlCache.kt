package com.dluvian.nozzle.data.cache

interface IClickedMediaUrlCache {
    fun insert(mediaUrl: String): Boolean
    fun contains(mediaUrl: String): Boolean
}