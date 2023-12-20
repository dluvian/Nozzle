package com.dluvian.nozzle.data.deletor

interface IDatabaseSweeper {
    suspend fun sweep()
}