package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.dluvian.nozzle.data.room.entity.HashtagEntity


@Dao
interface HashtagDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg hashtags: HashtagEntity)
}
