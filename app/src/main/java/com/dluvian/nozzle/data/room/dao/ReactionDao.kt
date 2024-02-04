package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.nozzle.data.room.entity.ReactionEntity
import com.dluvian.nozzle.model.EventId
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg reactionEntities: ReactionEntity)

    @Query(
        "SELECT eventId " +
                "FROM reaction " +
                "WHERE pubkey = :pubkey " +
                "AND eventId IN (:noteIds)"
    )
    suspend fun filterLikedNoteIds(noteIds: Collection<NoteId>, pubkey: Pubkey): List<String>

    @Query(
        "SELECT eventId " +
                "FROM reaction " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1) " +
                "AND eventId NOT IN (SELECT id FROM post)"
    )
    suspend fun getMissingEventIds(): List<EventId>

    @Query(
        "SELECT COUNT(*) " +
                "FROM reaction " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1) " +
                "AND eventId IN (SELECT id FROM post)"
    )
    fun getLikeCountFlow(): Flow<Int>

    @Query(
        "DELETE FROM reaction " +
                "WHERE pubkey NOT IN (SELECT pubkey FROM account) " +
                "OR eventId NOT IN (SELECT id FROM post)"
    )
    suspend fun deleteOrphaned(): Int

    @Query("DELETE FROM reaction WHERE eventId = :eventId")
    suspend fun deleteReaction(eventId: EventId)
}
