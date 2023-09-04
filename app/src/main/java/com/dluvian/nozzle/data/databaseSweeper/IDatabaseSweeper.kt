package com.dluvian.nozzle.data.databaseSweeper

interface IDatabaseSweeper {
    suspend fun sweep()
}