package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.MapInfo
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionDao {
    @Query(
        "INSERT OR IGNORE INTO reaction (eventId, pubkey) " +
                "VALUES (:eventId, :pubkey)"
    )
    suspend fun like(eventId: String, pubkey: String)

    @MapInfo(keyColumn = "eventId", valueColumn = "reactionCount")
    @Query(
        "SELECT eventId, COUNT(*) AS reactionCount " +
                "FROM reaction " +
                "WHERE eventId IN (:postIds) " +
                "GROUP BY eventId"
    )
    fun getNumOfLikesPerPostFlow(postIds: List<String>): Flow<Map<String, Int>>

    @Query(
        "SELECT eventId " +
                "FROM reaction " +
                "WHERE pubkey = :pubkey " +
                "AND eventId IN (:postIds)"
    )
    fun listLikedByFlow(pubkey: String, postIds: List<String>): Flow<List<String>>
}
