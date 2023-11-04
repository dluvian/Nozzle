package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.nozzle.data.room.entity.ReactionEntity

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg reactionEntities: ReactionEntity)

    @Query(
        "SELECT eventId " +
                "FROM reaction " +
                "WHERE pubkey = :pubkey " +
                "AND eventId IN (:postIds)"
    )
    suspend fun filterLikedPostIds(postIds: Collection<String>, pubkey: String): List<String>
}
