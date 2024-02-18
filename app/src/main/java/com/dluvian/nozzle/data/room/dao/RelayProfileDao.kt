package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.nozzle.data.room.entity.RelayProfileEntity
import com.dluvian.nozzle.model.Relay
import kotlinx.coroutines.flow.Flow

@Dao
interface RelayProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(vararg relayProfile: RelayProfileEntity)

    @Query("SELECT * FROM relayProfile WHERE relayUrl = :relayUrl")
    fun getRelayProfileFlow(relayUrl: Relay): Flow<RelayProfileEntity?>
}
