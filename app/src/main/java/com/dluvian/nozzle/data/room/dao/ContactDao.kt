package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.nozzle.data.TRUST_SCORE_BOOST
import com.dluvian.nozzle.data.room.entity.ContactEntity
import com.dluvian.nozzle.data.room.helper.FollowInfo
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow


@Dao
interface ContactDao {

    @Query(
        "SELECT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey = :pubkey"
    )
    suspend fun listContactPubkeys(pubkey: String): List<String>

    @Query(
        "SELECT pubkey " +
                "FROM contact " +
                "WHERE contactPubkey = :pubkey"
    )
    suspend fun listFollowedByPubkeys(pubkey: String): List<String>

    @Query(
        "SELECT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)"
    )
    fun listPersonalContactPubkeysFlow(): Flow<List<String>>

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
        "SELECT pubkey, MIN(createdAt) AS minCreatedAt " +
                "FROM contact " +
                "WHERE pubkey IN (:pubkeys)" +
                "GROUP BY pubkey"
    )
    suspend fun getTimestampByPubkey(pubkeys: Collection<String>): Map<
            @MapColumn("pubkey") Pubkey,
            @MapColumn("minCreatedAt") Long
            >

    @Transaction
    suspend fun insertAndDeleteOutdated(contacts: Collection<ContactEntity>) {
        if (contacts.isEmpty()) return

        // TODO: Refac: same as in Nip65Dao::insertAndDeleteOutdated
        val pubkeys = contacts.map(ContactEntity::pubkey).toSet()
        val timestamps = getTimestampByPubkey(pubkeys = pubkeys)
        val pubkeysToUpdate = contacts.filter { contact ->
            val createdAt = timestamps[contact.pubkey]
            createdAt == null || createdAt < contact.createdAt
        }.map { it.pubkey }
        if (pubkeysToUpdate.isNotEmpty()) delete(pubkeys = pubkeysToUpdate)

        val toInsert = contacts.filter { pubkeysToUpdate.contains(it.pubkey) }
            .groupBy { it.pubkey }
            .flatMap { (_, contactList) ->
                val maxCreatedAt = contactList.maxBy { it.createdAt }.createdAt
                contactList.filter { it.createdAt >= maxCreatedAt }
            }

        insertOrIgnore(*toInsert.toTypedArray())
    }

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE contactPubkey = :contactPubkey " +
                "AND pubkey IN (SELECT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1))"
    )
    fun getRawTrustScoreFlow(contactPubkey: String): Flow<Int>

    @MapInfo(keyColumn = "contactPubkey", valueColumn = "rawTrustScore")
    @Query(
        "SELECT contactPubkey, COUNT(*) AS rawTrustScore " +
                "FROM contact " +
                "WHERE contactPubkey IN (:contactPubkeys) " +
                "AND pubkey IN (SELECT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)) " +
                "GROUP BY contactPubkey"
    )
    fun getRawTrustScoreByPubkeyFlow(
        contactPubkeys: Collection<String>
    ): Flow<Map<String, Int>>

    @MapInfo(keyColumn = "contactPubkey", valueColumn = "rawTrustScore")
    @Query(
        "SELECT contactPubkey, COUNT(*) AS rawTrustScore " +
                "FROM contact " +
                "WHERE contactPubkey IN (:contactPubkeys) " +
                "AND pubkey IN (SELECT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)) " +
                "GROUP BY contactPubkey"
    )
    fun getRawTrustScoreByPubkey(contactPubkeys: Collection<String>): Map<String, Int>

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1) " +
                "AND contactPubkey IN (SELECT pubkey FROM contact)"
    )
    fun getTrustScoreDividerFlow(): Flow<Int>

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1) " +
                "AND contactPubkey IN (SELECT pubkey FROM contact)"
    )
    fun getTrustScoreDivider(): Int

    fun getTrustScoreFlow(
        contactPubkey: String
    ): Flow<Float> {
        val trustScoreDividerFlow = getTrustScoreDividerFlow().distinctUntilChanged()
        val rawTrustScoreFlow = getRawTrustScoreFlow(
            contactPubkey = contactPubkey
        ).distinctUntilChanged()
        return trustScoreDividerFlow
            .combine(rawTrustScoreFlow) { divider, rawTrustScore ->
                getTrustScorePercentage(
                    numOfFollowing = divider,
                    rawTrustScore = rawTrustScore
                )
            }
    }

    fun getTrustScoreByPubkeyFlow(
        contactPubkeys: Collection<String>
    ): Flow<Map<Pubkey, Float>> {
        if (contactPubkeys.isEmpty()) return flow { emit(emptyMap()) }

        val trustScoreDividerFlow = getTrustScoreDividerFlow().distinctUntilChanged()
        val rawTrustScorePerPubkeyFlow = getRawTrustScoreByPubkeyFlow(
            contactPubkeys = contactPubkeys
        ).distinctUntilChanged()
        return trustScoreDividerFlow
            .combine(rawTrustScorePerPubkeyFlow) { divider, rawTrustScorePerPubkey ->
                rawTrustScorePerPubkey.mapValues {
                    getTrustScorePercentage(
                        numOfFollowing = divider,
                        rawTrustScore = it.value
                    )
                }
            }.distinctUntilChanged()
    }

    fun getTrustScoreByPubkey(contactPubkeys: Collection<String>): Map<Pubkey, Float> {
        if (contactPubkeys.isEmpty()) return emptyMap()

        val trustScoreDivider = getTrustScoreDivider()
        val rawTrustScorePerPubkey = getRawTrustScoreByPubkey(contactPubkeys = contactPubkeys)

        return rawTrustScorePerPubkey.mapValues { (_, score) ->
            getTrustScorePercentage(
                numOfFollowing = trustScoreDivider,
                rawTrustScore = score
            )
        }
    }

    private fun getTrustScorePercentage(numOfFollowing: Int, rawTrustScore: Int): Float {
        val percentage = if (numOfFollowing <= 0) 0f else {
            val percentage = rawTrustScore.toFloat() / numOfFollowing
            if (percentage > 1f) 1f else percentage
        }
        return minOf(percentage * TRUST_SCORE_BOOST, 1f)
    }

    @Query(
        "DELETE FROM contact " +
                "WHERE pubkey NOT IN (SELECT pubkey FROM profile) " +
                "AND pubkey NOT IN (:exclude)" +
                "AND pubkey NOT IN (SELECT pubkey FROM account)" +
                "AND pubkey NOT IN (SELECT contactPubkey FROM contact WHERE pubkey IN (SELECT pubkey FROM account))"
    )
    suspend fun deleteOrphaned(exclude: Collection<String>): Int

    @Query("DELETE FROM contact WHERE pubkey IN (:pubkeys)")
    suspend fun delete(pubkeys: Collection<String>)

    @Query(
        "SELECT contactPubkey " +
                "FROM contact " +
                "WHERE contactPubkey IN (:contactPubkeys) " +
                "AND pubkey = (SELECT pubkey FROM account WHERE isActive = 1)"
    )
    suspend fun filterFriendsWithList(contactPubkeys: Collection<String>): List<String>

    @Query(
        "SELECT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1) " +
                "AND contactPubkey NOT IN (SELECT pubkey FROM profile)"
    )
    suspend fun listContactPubkeysWithMissingProfile(): List<Pubkey>


    @Query(
        "SELECT DISTINCT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey IN (" +
                "SELECT contactPubkey FROM contact WHERE pubkey = (" +
                "SELECT pubkey FROM account WHERE isActive = 1" +
                ")) " +
                "GROUP BY contactPubkey " +
                "ORDER BY COUNT(contactPubkey) DESC " +
                "LIMIT :limit"
    )
    fun listFriendsOfFriendsFlow(limit: Int): Flow<List<String>>

    @Query(
        "SELECT " +
                // SELECT numOfFollowing
                "(SELECT COUNT(contactPubkey) FROM contact WHERE pubkey = :pubkey) AS numOfFollowing, " +
                // SELECT numOfFollowers
                "(SELECT COUNT(pubkey) FROM contact WHERE contactPubkey = :pubkey) AS numOfFollowers, " +
                // SELECT followsYou
                "(SELECT EXISTS(SELECT * FROM contact WHERE pubkey = :pubkey " +
                "AND contactPubkey = (SELECT pubkey FROM account WHERE isActive = 1)" +
                ")) AS followsYou "
    )
    fun getFollowInfoFlow(pubkey: Pubkey): Flow<FollowInfo>
}
