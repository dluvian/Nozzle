package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.nozzle.data.room.entity.MentionEntity

@Dao
interface MentionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg mentions: MentionEntity)

    @Query(
        "DELETE FROM mention " +
                "WHERE eventId NOT IN (SELECT id FROM post) " +
                "AND pubkey NOT IN (SELECT pubkey FROM profile)"
    )
    suspend fun deleteOrphaned(): Int
}
