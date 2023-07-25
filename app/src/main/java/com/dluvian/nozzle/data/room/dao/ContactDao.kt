package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.ContactEntity
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDebounce
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
        contactPubkeys: List<String>
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
            .firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()
        val rawTrustScoreFlow = getRawTrustScoreFlow(
            pubkey = pubkey,
            contactPubkey = contactPubkey
        ).firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()
        return trustScoreDividerFlow
            .combine(rawTrustScoreFlow) { divider, rawTrustScore ->
                getPercentage(
                    numOfFollowing = divider,
                    rawTrustScore = rawTrustScore
                )
            }
    }

    fun getTrustScorePerPubkeyFlow(
        pubkey: String,
        contactPubkeys: List<String>
    ): Flow<Map<String, Float>> {
        val trustScoreDividerFlow = getTrustScoreDividerFlow(pubkey)
            .firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()
        val rawTrustScorePerPubkeyFlow = getRawTrustScorePerPubkeyFlow(
            pubkey = pubkey,
            contactPubkeys = contactPubkeys
        ).firstThenDebounce(NORMAL_DEBOUNCE)
            .distinctUntilChanged()
        return trustScoreDividerFlow
            .combine(rawTrustScorePerPubkeyFlow) { divider, rawTrustScorePerPubkey ->
                rawTrustScorePerPubkey.mapValues {
                    getPercentage(
                        numOfFollowing = divider,
                        rawTrustScore = it.value
                    )
                }
            }
    }

    private fun getPercentage(numOfFollowing: Int, rawTrustScore: Int): Float {
        return if (numOfFollowing <= 0) 0f else {
            val percentage = rawTrustScore.toFloat() / numOfFollowing
            if (percentage > 1f) 1f else percentage
        }
    }
}
