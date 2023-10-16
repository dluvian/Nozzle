package com.dluvian.nozzle.data.databaseSweeper

import android.util.Log
import com.dluvian.nozzle.data.cache.IIdCache
import com.dluvian.nozzle.data.provider.IContactListProvider
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.AppDatabase
import kotlin.random.Random

private const val TAG = "DatabaseSweeper"


// TODO: Check if this works once you have internet again
class DatabaseSweeper(
    private val keepPosts: Int,
    private val pubkeyProvider: IPubkeyProvider,
    private val contactListProvider: IContactListProvider,
    private val dbSweepExcludingCache: IIdCache,
    private val database: AppDatabase
) : IDatabaseSweeper {
    override suspend fun sweep() {
        Log.i(TAG, "Sweep database")
        val excludePubkeys = dbSweepExcludingCache.getPubkeys() +
                contactListProvider.listPersonalContactPubkeys() +
                pubkeyProvider.getPubkey()

        when (Random.nextInt(until = 4)) {
            0 -> deletePosts()
            1 -> deleteProfiles(excludePubkeys = excludePubkeys)
            2 -> deleteContactLists(excludePubkeys = excludePubkeys)
            3 -> deleteNip65(excludePubkeys = excludePubkeys)
            else -> Log.w(TAG, "Delete case not covered")
        }
    }

    private suspend fun deletePosts() {
        // TODO: Exclude author via db table of user acc
        val deletePostCount = database.postDao().deleteAllExceptNewest(
            amountToKeep = keepPosts,
            exclude = dbSweepExcludingCache.getPostIds(),
            excludeAuthor = pubkeyProvider.getPubkey()
        )
        Log.i(TAG, "Deleted $deletePostCount posts")
    }

    private suspend fun deleteProfiles(excludePubkeys: Collection<String>) {
        val deleteProfileCount = database.profileDao().deleteOrphaned(exclude = excludePubkeys)
        Log.i(TAG, "Deleted $deleteProfileCount profiles")
    }

    private suspend fun deleteContactLists(excludePubkeys: Collection<String>) {
        val deleteContactCount = database.contactDao().deleteOrphaned(exclude = excludePubkeys)
        Log.i(TAG, "Deleted $deleteContactCount contact entries")
    }

    private suspend fun deleteNip65(excludePubkeys: Collection<String>) {
        val deleteNip65Count = database.nip65Dao().deleteOrphaned(excludePubkeys = excludePubkeys)
        Log.i(TAG, "Deleted $deleteNip65Count nip65 entries")
    }
}