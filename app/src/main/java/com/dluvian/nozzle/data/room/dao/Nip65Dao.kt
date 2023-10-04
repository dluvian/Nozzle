package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import kotlinx.coroutines.flow.Flow


@Dao
interface Nip65Dao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg nip65Entries: Nip65Entity)

    @Query(
        "DELETE FROM nip65 " +
                "WHERE pubkey = :pubkey AND createdAt < :newTimestamp"
    )
    suspend fun deleteIfOutdated(pubkey: String, newTimestamp: Long)

    @Transaction
    suspend fun insertAndDeleteOutdated(
        pubkey: String,
        timestamp: Long,
        vararg nip65Entities: Nip65Entity
    ) {
        if (nip65Entities.isEmpty()) return

        deleteIfOutdated(pubkey = pubkey, newTimestamp = timestamp)
        insertOrIgnore(*nip65Entities)
    }

    @MapInfo(keyColumn = "url", valueColumn = "pubkey")
    @Query(
        "SELECT pubkey, url " +
                "FROM nip65 " +
                "WHERE isWrite = '1' " +
                "AND pubkey IN (:pubkeys)"
    )
    suspend fun getPubkeysByWriteRelays(pubkeys: Collection<String>): Map<Relay, Set<Pubkey>>

    @MapInfo(keyColumn = "pubkey", valueColumn = "url")
    @Query(
        "SELECT pubkey, url " +
                "FROM nip65 " +
                "WHERE isWrite = '1' " +
                "AND pubkey IN (:pubkeys)"
    )
    suspend fun getWriteRelaysOfPubkeys(pubkeys: Collection<String>): Map<Pubkey, List<Relay>>

    @Query(
        "SELECT url " +
                "FROM nip65 " +
                "WHERE isRead = '1' " +
                "AND pubkey = :pubkey"
    )
    suspend fun getReadRelaysOfPubkey(pubkey: String): List<String>

    @Query(
        "SELECT url " +
                "FROM nip65 " +
                "WHERE isWrite = '1' " +
                "AND pubkey = :pubkey"
    )
    suspend fun getWriteRelaysOfPubkey(pubkey: String): List<String>

    @Query(
        "SELECT * " +
                "FROM nip65 " +
                "WHERE pubkey = :pubkey"
    )
    fun getRelaysOfPubkeyFlow(pubkey: String): Flow<List<Nip65Entity>>

    // TODO: No exclude. This should exclude pubkeys in user acc table
    @Query(
        "DELETE FROM nip65 " +
                "WHERE pubkey NOT IN (SELECT pubkey FROM profile) " +
                "AND pubkey NOT IN (:excludePubkeys)"
    )
    suspend fun deleteOrphaned(excludePubkeys: Collection<String>): Int

    @Query("SELECT DISTINCT(pubkey) FROM nip65 WHERE pubkey IN (:pubkeys)")
    suspend fun filterPubkeysWithNip65(pubkeys: Collection<String>): List<String>
}
