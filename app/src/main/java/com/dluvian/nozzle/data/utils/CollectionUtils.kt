package com.dluvian.nozzle.data.utils

fun <T, U> MutableMap<T, MutableSet<U>>.addOrCreate(key: T, itemToAdd: U) {
    val present = this.putIfAbsent(key, mutableSetOf(itemToAdd))
    present?.add(itemToAdd)
}
