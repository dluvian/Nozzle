package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.nozzle.data.room.entity.RepostEntity
import com.dluvian.nozzle.model.EventId

@Dao
interface RepostDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg reposts: RepostEntity)

    @Query("SELECT EXISTS(SELECT eventId FROM repost WHERE eventId = :eventId)")
    suspend fun isRepost(eventId: EventId): Boolean
}
