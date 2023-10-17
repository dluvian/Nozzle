package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.dluvian.nozzle.data.room.entity.MentionEntity

@Dao
interface MentionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg mentions: MentionEntity)
}
