package com.dluvian.nozzle.data.deletor

import android.util.Log
import com.dluvian.nozzle.data.MAX_SQL_PARAMS
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

    override suspend fun sweep() {
        if (!isSweeping.compareAndSet(false, true)) {
            Log.i(TAG, "Sweep blocked by ongoing sweep")
            return
        }
        Log.i(TAG, "Sweep database")

        when (Random.nextInt(until = 6)) {
            0 -> deletePosts()
            1 -> deleteOwnPosts()
            2 -> deleteProfiles()
            3 -> deleteContactLists()
            4 -> deleteNip65()
            5 -> deleteReactions()
            else -> Log.w(TAG, "Delete case not covered")
        }
        isSweeping.set(false)
    }

    private suspend fun deletePosts() {
        val deletePostCount = database.postDao().deleteExceptNewestAndOwn(
            amountToKeep = keepPosts,
            exclude = dbSweepExcludingCache.getPostIds().take(MAX_SQL_PARAMS),
        )
        dbSweepExcludingCache.clearPostIds()
        Log.i(TAG, "Deleted $deletePostCount posts")
    }

    private suspend fun deleteOwnPosts() {
        val deletePostCount = database.postDao().deleteOwnPosts(amountToKeep = keepPosts)
        Log.i(TAG, "Deleted $deletePostCount of your own posts")
    }

    private suspend fun deleteProfiles() {
        val deleteProfileCount = database
            .profileDao()
            .deleteOrphaned(exclude = dbSweepExcludingCache.getPubkeys().take(MAX_SQL_PARAMS))
        dbSweepExcludingCache.clearPubkeys()
        Log.i(TAG, "Deleted $deleteProfileCount profiles")
    }

    private suspend fun deleteContactLists() {
        val deleteContactCount = database
            .contactDao()
            .deleteOrphaned(
                exclude = dbSweepExcludingCache.getContactListAuthors().take(MAX_SQL_PARAMS)
            )
        dbSweepExcludingCache.clearContactListAuthors()
        Log.i(TAG, "Deleted $deleteContactCount contact entries")
    }

    private suspend fun deleteNip65() {
        val deleteNip65Count = database
            .nip65Dao()
            .deleteOrphaned(dbSweepExcludingCache.getNip65Authors().take(MAX_SQL_PARAMS))
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
