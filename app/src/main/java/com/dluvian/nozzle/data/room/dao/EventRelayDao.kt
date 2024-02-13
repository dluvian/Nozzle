package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dluvian.nozzle.data.room.entity.EventRelayEntity
import com.dluvian.nozzle.model.CountedRelayUsage
import com.dluvian.nozzle.model.EventId
import com.dluvian.nozzle.model.NoteId
import com.dluvian.nozzle.model.Relay
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

    @MapInfo(keyColumn = "eventId", valueColumn = "relayUrl")
    @Query(
        "SELECT eventId, relayUrl " +
                "FROM eventRelay " +
                "WHERE eventId IN " +
                // My replies to :currentId
                "(SELECT id " +
                "FROM post " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1) " +
                "AND replyToId = :currentId)"

    )
    fun getRelaysOfPersonalRepliesFlow(currentId: NoteId): Flow<Map<String, List<String>>>

    @Query(
        "SELECT relayUrl " +
                "FROM eventRelay " +
                "WHERE eventId = :eventId"
    )
    fun listSeenInRelays(eventId: EventId): List<Relay>

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

    @Query(
        "SELECT DISTINCT(relayUrl) " +
                "FROM eventRelay " +
                "GROUP BY relayUrl " +
                "ORDER BY COUNT(relayUrl) DESC " +
                "LIMIT :limit"
    )
    suspend fun getAllSortedByNumOfEvents(limit: Int): List<Relay>
}
