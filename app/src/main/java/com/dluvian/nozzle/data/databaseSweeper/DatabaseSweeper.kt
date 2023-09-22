package com.dluvian.nozzle.data.databaseSweeper

import android.util.Log
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.AppDatabase

private const val TAG = "DatabaseSweeper"

class DatabaseSweeper(
    private val keepPosts: Int,
    private val thresholdFactor: Float,
    private val pubkeyProvider: IPubkeyProvider,
    private val contactListProvider: IContactListProvider,
    private val dbSweepExcludingCache: IIdCache,
    private val database: AppDatabase
) : IDatabaseSweeper {
    override suspend fun sweep() {
        val postCount = database.postDao().countPosts()
        if (postCount < thresholdFactor * keepPosts) {
            Log.i(TAG, "Skip sweep. Only $postCount posts in db")
            return
        }
        val excludePostIds = dbSweepExcludingCache.getPostIds()
        val excludeProfiles = dbSweepExcludingCache.getPubkeys()
        Log.i(
            TAG,
            "Sweep database leaving $keepPosts, " +
                    "excluding ${excludePostIds.size} posts " +
                    "and ${excludeProfiles.size} profiles"
        )

        val start = System.currentTimeMillis()
        delete(excludePostIds = excludePostIds, excludeProfiles = excludeProfiles)
        dbSweepExcludingCache.clearAll()
        Log.i(TAG, "Execution time ${System.currentTimeMillis() - start}ms")
    }

    private suspend fun delete(
        excludePostIds: Collection<String>,
        excludeProfiles: Collection<String>
    ) {
        // TODO: Exclude contacts via db table of user acc
        val contacts = contactListProvider.listPersonalContactPubkeys() + pubkeyProvider.getPubkey()
        // TODO: Exclude author via db table of user acc
        val deletePostCount = database.postDao().deleteAllExceptNewest(
            amountToKeep = keepPosts,
            exclude = excludePostIds,
            excludeAuthor = pubkeyProvider.getPubkey()
        )
        Log.i(TAG, "Deleted $deletePostCount posts")
        if (deletePostCount == 0) return

        val deleteRelayCount = database.eventRelayDao().deleteOrphaned()
        Log.i(TAG, "Deleted $deleteRelayCount event relay entries")

        val deleteReactionCount = database.reactionDao().deleteOrphaned()
        Log.i(TAG, "Deleted $deleteReactionCount reactions")

        val deleteProfileCount = database.profileDao().deleteOrphaned(
            exclude = excludeProfiles + contacts
        )
        Log.i(TAG, "Deleted $deleteProfileCount profiles")

        val deleteContactCount = database.contactDao().deleteOrphaned(exclude = contacts)
        Log.i(TAG, "Deleted $deleteContactCount contact entries")

        // TODO: Exclude author via db table of user acc
        val deleteNip65Count = database.nip65Dao().deleteOrphaned(excludePubkeys = contacts)
        Log.i(TAG, "Deleted $deleteNip65Count nip65 entries")
    }
}