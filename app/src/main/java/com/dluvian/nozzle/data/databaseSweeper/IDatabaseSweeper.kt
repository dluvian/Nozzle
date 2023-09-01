package com.dluvian.nozzle.data.databaseSweeper

interface IDatabaseSweeper {
    suspend fun sweep(
        keepPosts: Int,
        excludePostIds: Collection<String>,
        excludeProfiles: Collection<String>
    )
}