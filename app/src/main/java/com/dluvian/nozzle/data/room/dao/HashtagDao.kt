package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.nozzle.data.room.entity.HashtagEntity


@Dao
interface HashtagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg hashtags: HashtagEntity)


    @Query(
        "DELETE FROM hashtag " +
                "WHERE eventId NOT IN (SELECT id FROM post)"
    )
    suspend fun deleteOrphaned(): Int
}
