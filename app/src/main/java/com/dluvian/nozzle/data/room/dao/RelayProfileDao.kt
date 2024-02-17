package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.dluvian.nozzle.data.room.entity.RelayProfileEntity

@Dao
interface RelayProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(vararg relayProfile: RelayProfileEntity)
}
