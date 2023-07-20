package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.ContactEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged


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
    suspend fun countFollowing(pubkey: String): Int

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE pubkey = :pubkey"
    )
    fun countFollowingFlow(pubkey: String): Flow<Int>

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE contactPubkey = :pubkey"
    )
    suspend fun countFollowers(pubkey: String): Int

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE contactPubkey = :pubkey"
    )
    fun countFollowersFlow(pubkey: String): Flow<Int>

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE contactPubkey = :contactPubkey " +
                "AND pubkey IN (SELECT contactPubkey FROM contact WHERE pubkey = :pubkey)"
    )
    fun countFriendFollowersFlow(pubkey: String, contactPubkey: String): Flow<Int>

    @MapInfo(keyColumn = "contactPubkey", valueColumn = "friendFollowerCount")
    @Query(
        "SELECT contactPubkey, COUNT(*) AS friendFollowerCount " +
                "FROM contact " +
                "WHERE contactPubkey IN (:contactPubkeys) " +
                "AND pubkey IN (SELECT contactPubkey FROM contact WHERE pubkey = :pubkey) " +
                "GROUP BY contactPubkey"
    )
    fun countFriendFollowersPerPubkeyFlow(
        pubkey: String,
        contactPubkeys: List<String>
    ): Flow<Map<String, Int>>

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

    fun getFollowedByFriendsPercentageFlow(
        pubkey: String,
        contactPubkey: String
    ): Flow<Float> {
        val numOfFollowingFlow = countFollowingFlow(pubkey).distinctUntilChanged()
        val numOfFriendFollowersFlow = countFriendFollowersFlow(
            pubkey = pubkey,
            contactPubkey = contactPubkey
        ).distinctUntilChanged()
        return numOfFollowingFlow
            .combine(numOfFriendFollowersFlow) { numOfFollowing, numOfFriendFollowing ->
                getPercentage(
                    numOfFollowing = numOfFollowing,
                    numOfFriendFollowing = numOfFriendFollowing
                )
            }
    }

    fun getFollowedByFriendsPercentagePerPubkeyFlow(
        pubkey: String,
        contactPubkeys: List<String>
    ): Flow<Map<String, Float>> {
        val numOfFollowingFlow = countFollowingFlow(pubkey).distinctUntilChanged()
        val numOfFriendFollowersPerPubkeyFlow = countFriendFollowersPerPubkeyFlow(
            pubkey = pubkey,
            contactPubkeys = contactPubkeys
        ).distinctUntilChanged()
        return numOfFollowingFlow
            .combine(numOfFriendFollowersPerPubkeyFlow) { numOfFollowing, numOfFriendFollowingPerPubkey ->
                numOfFriendFollowingPerPubkey.mapValues {
                    getPercentage(
                        numOfFollowing = numOfFollowing,
                        numOfFriendFollowing = it.value
                    )
                }
            }
    }

    private fun getPercentage(numOfFollowing: Int, numOfFriendFollowing: Int): Float {
        return if (numOfFollowing <= 0) 0f else {
            val percentage = numOfFriendFollowing.toFloat() / numOfFollowing
            if (percentage > 1f) 1f else percentage
        }
    }
}
