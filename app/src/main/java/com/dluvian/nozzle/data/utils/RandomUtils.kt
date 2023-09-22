package com.dluvian.nozzle.data.utils

fun <T> List<T>.takeRandom80percent(): List<T> {
    if (isEmpty()) return emptyList()

    return this.shuffled().take((0.8 * size).toInt())
}