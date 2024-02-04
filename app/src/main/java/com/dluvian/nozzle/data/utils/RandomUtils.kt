package com.dluvian.nozzle.data.utils

import kotlin.random.Random

fun <T> List<T>.takeRandom80percent(): List<T> {
    if (isEmpty()) return emptyList()
    return this.filter { _ -> get80PercentTrue() }
}

fun get80PercentTrue() = Random.nextInt(10) < 8
