package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface ReactionDao {
    @Query(
        "INSERT OR IGNORE INTO reaction (eventId, pubkey) " +
                "VALUES (:eventId, :pubkey)"
    )
    suspend fun like(eventId: String, pubkey: String)

    @Query(
        "DELETE FROM reaction " +
                "WHERE eventId NOT IN (SELECT id FROM post)"
    )
    suspend fun deleteOrphaned(): Int

    @Query(
        "SELECT eventId " +
                "FROM reaction " +
                "WHERE pubkey = :pubkey " +
                "AND eventId IN (:postIds)"
    )
    suspend fun filterLikedPostIds(postIds: Collection<String>, pubkey: String): List<String>
}
