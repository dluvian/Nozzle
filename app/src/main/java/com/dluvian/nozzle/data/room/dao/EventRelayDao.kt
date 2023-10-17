package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.MapInfo
import androidx.room.Query
import com.dluvian.nozzle.model.CountedRelayUsage
import kotlinx.coroutines.flow.Flow


// TODO: Remove deprecated @MapInfo

@Dao
interface EventRelayDao {
    @Query(
        "INSERT OR IGNORE INTO eventRelay (eventId, relayUrl) " +
                "VALUES (:eventId, :relayUrl)"
    )
    suspend fun insertOrIgnore(eventId: String, relayUrl: String)

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
