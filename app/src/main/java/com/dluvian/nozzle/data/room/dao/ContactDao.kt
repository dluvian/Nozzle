package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.TRUST_SCORE_BOOST
import com.dluvian.nozzle.data.room.entity.ContactEntity
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
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
        "SELECT contactPubkey " +
                "FROM contact " +
                "WHERE pubkey = :pubkey"
    )
    fun listContactPubkeysFlow(pubkey: String): Flow<List<String>>

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

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE contactPubkey = :contactPubkey " +
                "AND pubkey IN (SELECT contactPubkey FROM contact WHERE pubkey = :pubkey)"
    )
    fun getRawTrustScoreFlow(pubkey: String, contactPubkey: String): Flow<Int>

    @MapInfo(keyColumn = "contactPubkey", valueColumn = "rawTrustScore")
    @Query(
        "SELECT contactPubkey, COUNT(*) AS rawTrustScore " +
                "FROM contact " +
                "WHERE contactPubkey IN (:contactPubkeys) " +
                "AND pubkey IN (SELECT contactPubkey FROM contact WHERE pubkey = :pubkey) " +
                "GROUP BY contactPubkey"
    )
    fun getRawTrustScorePerPubkeyFlow(
        pubkey: String,
        contactPubkeys: Collection<String>
    ): Flow<Map<String, Int>>

    @Query(
        "SELECT COUNT(*) " +
                "FROM contact " +
                "WHERE pubkey = :pubkey " +
                "AND contactPubkey IN (SELECT pubkey FROM contact)"
    )
    fun getTrustScoreDividerFlow(pubkey: String): Flow<Int>

    fun getTrustScoreFlow(
        pubkey: String,
        contactPubkey: String
    ): Flow<Float> {
        val trustScoreDividerFlow = getTrustScoreDividerFlow(pubkey)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
        val rawTrustScoreFlow = getRawTrustScoreFlow(
            pubkey = pubkey,
            contactPubkey = contactPubkey
        ).firstThenDistinctDebounce(NORMAL_DEBOUNCE)
        return trustScoreDividerFlow
            .combine(rawTrustScoreFlow) { divider, rawTrustScore ->
                getTrustScorePercentage(
                    numOfFollowing = divider,
                    rawTrustScore = rawTrustScore
                )
            }
    }

    fun getTrustScorePerPubkeyFlow(
        pubkey: String,
        contactPubkeys: Collection<String>
    ): Flow<Map<String, Float>> {
        if (contactPubkeys.isEmpty()) return flow { emit(emptyMap()) }

        val trustScoreDividerFlow = getTrustScoreDividerFlow(pubkey).distinctUntilChanged()
        val rawTrustScorePerPubkeyFlow = getRawTrustScorePerPubkeyFlow(
            pubkey = pubkey,
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

    private fun getTrustScorePercentage(numOfFollowing: Int, rawTrustScore: Int): Float {
        val percentage = if (numOfFollowing <= 0) 0f else {
            val percentage = rawTrustScore.toFloat() / numOfFollowing
            if (percentage > 1f) 1f else percentage
        }
        return minOf(percentage * TRUST_SCORE_BOOST, 1f)
    }
}
