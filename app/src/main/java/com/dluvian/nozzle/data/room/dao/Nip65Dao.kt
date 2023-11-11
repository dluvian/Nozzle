package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.Nip65Entity
import com.dluvian.nozzle.data.room.helper.Nip65Relay
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.Relay
import kotlinx.coroutines.flow.Flow


@Dao
interface Nip65Dao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg nip65Entries: Nip65Entity)

    @Query("DELETE FROM nip65 WHERE pubkey IN (:pubkeys)")
    suspend fun delete(pubkeys: Collection<Pubkey>)

    @Query(
        "SELECT pubkey, MAX(createdAt) as maxCreatedAt " +
                "FROM nip65 " +
                "WHERE pubkey IN (:pubkeys)" +
                "GROUP BY pubkey"
    )
    suspend fun getTimestampByPubkey(pubkeys: Collection<String>): Map<
            @MapColumn("pubkey") Pubkey,
            @MapColumn("maxCreatedAt") Long
            >

    @Transaction
    suspend fun insertAndDeleteOutdated(nip65s: Collection<Nip65Entity>) {
        if (nip65s.isEmpty()) return

        val pubkeys = nip65s.map(Nip65Entity::pubkey).toSet()
        val timestamps = getTimestampByPubkey(pubkeys = pubkeys)
        val outdatedPubkeys = nip65s
            .filter { it.createdAt > (timestamps[it.pubkey] ?: Long.MAX_VALUE) }
            .map { it.pubkey }
            .toSet()

        if (outdatedPubkeys.isNotEmpty()) delete(pubkeys = outdatedPubkeys)

        insertOrIgnore(*nip65s.toTypedArray())
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
        "SELECT url, isRead, isWrite " +
                "FROM nip65 " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)"
    )
    fun getPersonalRelaysFlow(): Flow<List<Nip65Relay>>

    @Query(
        "DELETE FROM nip65 " +
                "WHERE pubkey NOT IN (SELECT pubkey FROM profile) " +
                "AND pubkey NOT IN (:excludePubkeys) " +
                "AND pubkey NOT IN (SELECT pubkey FROM account)" +
                "AND pubkey NOT IN (SELECT contactPubkey FROM contact WHERE pubkey IN (SELECT pubkey FROM account))"
    )
    suspend fun deleteOrphaned(excludePubkeys: Collection<String>): Int

    @Query("SELECT DISTINCT(pubkey) FROM nip65 WHERE pubkey IN (:pubkeys)")
    suspend fun filterPubkeysWithNip65(pubkeys: Collection<String>): List<String>

    @Query(
        "SELECT url " +
                "FROM nip65 " +
                "WHERE pubkey IN (:pubkeys) " +
                "GROUP BY url " +
                "ORDER BY COUNT(url) DESC"
    )
    suspend fun getRelaysOfPubkeys(pubkeys: Collection<String>): List<Relay>
}
