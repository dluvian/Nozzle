package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.nozzle.data.room.entity.EventRelayEntity
import com.dluvian.nozzle.model.CountedRelayUsage
import kotlinx.coroutines.flow.Flow


// TODO: Remove deprecated @MapInfo

@Dao
interface EventRelayDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg eventRelays: EventRelayEntity)

    @MapInfo(keyColumn = "eventId", valueColumn = "relayUrl")
    @Query(
        "SELECT eventId, relayUrl " +
                "FROM eventRelay " +
                "WHERE eventId IN (:eventIds)"
    )
    fun getRelaysPerEventIdMapFlow(eventIds: Collection<String>): Flow<Map<String, List<String>>>

    @Query(
        "SELECT post.pubkey, eventRelay.relayUrl, COUNT(post.id) AS numOfPosts " +
                "FROM eventRelay " +
                "JOIN post ON post.id = eventRelay.eventId " +
                "WHERE post.pubkey IN (:pubkeys) " +
                "GROUP BY post.pubkey, eventRelay.relayUrl " +
                "HAVING numOfPosts > 0"
    )
    suspend fun getCountedRelaysPerPubkey(pubkeys: Collection<String>): List<CountedRelayUsage>

    @Query(
        "SELECT DISTINCT(relayUrl) " +
                "FROM eventRelay " +
                "WHERE eventId IN " +
                "(SELECT id FROM post WHERE pubkey = :pubkey) "
    )
    fun listUsedRelaysFlow(pubkey: String): Flow<List<String>>
}
