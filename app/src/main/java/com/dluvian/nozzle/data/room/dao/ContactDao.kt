package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.ContactEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface ContactDao {

    @Query(
        "SELECT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey = :pubkey"
    )
    suspend fun listContactPubkeys(pubkey: String): List<String>

    @Query(
        "SELECT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey = :pubkey"
    )
    fun listContactPubkeysFlow(pubkey: String): Flow<List<String>>

    @Query(
        "SELECT * " +
                "FROM contact " +
                "WHERE pubkey = :pubkey"
    )
    suspend fun listContacts(pubkey: String): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg contacts: ContactEntity)

    @Query(
        "DELETE FROM contact " +
                "WHERE pubkey = :pubkey AND contactPubkey = :contactPubkey"
    )
    suspend fun deleteContact(pubkey: String, contactPubkey: String)

    @Query(
        "UPDATE contact " +
                "SET createdAt = :createdAt " +
                "WHERE pubkey = :pubkey"
    )
    suspend fun updateTime(pubkey: String, createdAt: Long)

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE pubkey = :pubkey"
    )
    suspend fun getNumberOfFollowing(pubkey: String): Int

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE pubkey = :pubkey"
    )
    fun getNumberOfFollowingFlow(pubkey: String): Flow<Int>

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE contactPubkey = :pubkey"
    )
    suspend fun getNumberOfFollowers(pubkey: String): Int

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE contactPubkey = :pubkey"
    )
    fun getNumberOfFollowersFlow(pubkey: String): Flow<Int>

    @Query(
        "SELECT EXISTS(SELECT * " +
                "FROM contact " +
                "WHERE pubkey = :pubkey AND contactPubkey = :contactPubkey)"
    )
    suspend fun isFollowed(pubkey: String, contactPubkey: String): Boolean

    @Query(
        "SELECT EXISTS(SELECT * " +
                "FROM contact " +
                "WHERE pubkey = :pubkey AND contactPubkey = :contactPubkey)"
    )
    fun isFollowedFlow(pubkey: String, contactPubkey: String): Flow<Boolean>

    @Query(
        "DELETE FROM contact " +
                "WHERE pubkey = :pubkey AND createdAt < :newTimestamp"
    )
    suspend fun deleteIfOutdated(pubkey: String, newTimestamp: Long)

    @Transaction
    suspend fun insertAndDeleteOutdated(
        pubkey: String,
        newTimestamp: Long,
        vararg contacts: ContactEntity
    ) {
        deleteIfOutdated(pubkey = pubkey, newTimestamp = newTimestamp)
        insertOrIgnore(*contacts)
    }
}
