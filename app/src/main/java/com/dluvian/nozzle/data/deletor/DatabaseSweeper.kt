package com.dluvian.nozzle.data.deletor

import android.util.Log
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.room.AppDatabase
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.random.Random

private const val TAG = "DatabaseSweeper"

class DatabaseSweeper(
    private val keepPosts: Int,
    private val dbSweepExcludingCache: IIdCache,
    private val database: AppDatabase
) : IDatabaseSweeper {
    private val isSweeping = AtomicBoolean(false)
    private val maxSQLParams = 250
    override suspend fun sweep() {
        if (!isSweeping.compareAndSet(false, true)) {
            Log.i(TAG, "Sweep blocked by ongoing sweep")
            return
        }
        Log.i(TAG, "Sweep database")

        when (Random.nextInt(until = 5)) {
            0 -> deletePosts()
            1 -> deleteProfiles()
            2 -> deleteContactLists()
            3 -> deleteNip65()
            4 -> deleteReactions()
            else -> Log.w(TAG, "Delete case not covered")
        }
        isSweeping.set(false)
    }

    private suspend fun deletePosts() {
        val deletePostCount = database.postDao().deleteAllExceptNewest(
            amountToKeep = keepPosts,
            exclude = dbSweepExcludingCache.getPostIds().take(maxSQLParams),
        )
        dbSweepExcludingCache.clearPostIds()
        Log.i(TAG, "Deleted $deletePostCount posts")
    }

    private suspend fun deleteProfiles() {
        val deleteProfileCount = database
            .profileDao()
            .deleteOrphaned(exclude = dbSweepExcludingCache.getPubkeys().take(maxSQLParams))
        dbSweepExcludingCache.clearPubkeys()
        Log.i(TAG, "Deleted $deleteProfileCount profiles")
    }

    private suspend fun deleteContactLists() {
        val deleteContactCount = database
            .contactDao()
            .deleteOrphaned(
                exclude = dbSweepExcludingCache.getContactListAuthors().take(maxSQLParams)
            )
        dbSweepExcludingCache.clearContactListAuthors()
        Log.i(TAG, "Deleted $deleteContactCount contact entries")
    }

    private suspend fun deleteNip65() {
        val deleteNip65Count = database
            .nip65Dao()
            .deleteOrphaned(dbSweepExcludingCache.getNip65Authors().take(maxSQLParams))
        dbSweepExcludingCache.clearNip65Authors()
        Log.i(TAG, "Deleted $deleteNip65Count nip65 entries")
    }

    private suspend fun deleteReactions() {
        val deleteReactionCount = database
            .reactionDao()
            .deleteOrphaned()
        Log.i(TAG, "Deleted $deleteReactionCount reactions")
    }
}
