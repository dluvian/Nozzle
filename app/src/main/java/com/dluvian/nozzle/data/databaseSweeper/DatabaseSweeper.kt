package com.dluvian.nozzle.data.databaseSweeper

import android.util.Log
import com.dluvian.nozzle.data.provider.IPubkeyProvider
import com.dluvian.nozzle.data.room.dao.ContactDao
import com.dluvian.nozzle.data.room.dao.EventRelayDao
import com.dluvian.nozzle.data.room.dao.Nip65Dao
import com.dluvian.nozzle.data.room.dao.PostDao
import com.dluvian.nozzle.data.room.dao.ProfileDao
import com.dluvian.nozzle.data.room.dao.ReactionDao

private const val TAG = "DatabaseSweeper"

class DatabaseSweeper(
    private val postDao: PostDao,
    private val eventRelayDao: EventRelayDao,
    private val reactionDao: ReactionDao,
    private val profileDao: ProfileDao,
    private val contactDao: ContactDao,
    private val nip65Dao: Nip65Dao,
    private val pubkeyProvider: IPubkeyProvider,

    ) : IDatabaseSweeper {
    override suspend fun sweep(
        keepPosts: Int,
        excludePostIds: Collection<String>,
        excludeProfiles: Collection<String>,
    ) {
        Log.i(
            TAG,
            "Sweep database leaving $keepPosts, " +
                    "excluding ${excludePostIds.size} posts " +
                    "and ${excludeProfiles.size} profiles"
        )

        val deletePostCount = postDao.deleteAllExceptNewest(
            amountToKeep = keepPosts,
            exlude = excludePostIds
        )
        if (deletePostCount == 0) return
        Log.i(TAG, "Deleted $deletePostCount posts")

        val deleteRelayCount = eventRelayDao.deleteOrphaned()
        Log.i(TAG, "Deleted $deleteRelayCount event relay entries")

        val deleteReactionCount = reactionDao.deleteOrphaned()
        Log.i(TAG, "Deleted $deleteReactionCount reactions")

        val deleteProfileCount = profileDao.deleteOrphaned(
            exclude = excludeProfiles + pubkeyProvider.getPubkey()
        )
        Log.i(TAG, "Deleted $deleteProfileCount profiles")

        val deleteContactCount = contactDao.deleteOrphaned()
        Log.i(TAG, "Deleted $deleteContactCount contact entries")

        val deleteNip65Count = nip65Dao.deleteOrphaned()
        Log.i(TAG, "Deleted $deleteNip65Count nip65 entries")
    }

}