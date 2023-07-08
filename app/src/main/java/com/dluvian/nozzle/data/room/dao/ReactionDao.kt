package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionDao {
    @Query(
        "INSERT OR IGNORE INTO reaction (eventId, pubkey) " +
                "VALUES (:eventId, :pubkey)"
    )
    suspend fun like(eventId: String, pubkey: String)

    @Query(
        "SELECT eventId " +
                "FROM reaction " +
                "WHERE pubkey = :pubkey " +
                "AND eventId IN (:postIds)"
    )
    fun listLikedByFlow(pubkey: String, postIds: List<String>): Flow<List<String>>
}
