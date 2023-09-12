package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.room.helper.extended.ProfileEntityExtended
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE pubkey = :pubkey")
    suspend fun getProfile(pubkey: String): ProfileEntity?

    @Query(
        // SELECT metadata
        "SELECT mainProfile.*, " +
                // SELECT isFollowedByMe
                "(EXISTS (SELECT contactPubkey FROM contact " +
                "WHERE pubkey = :personalPubkey AND contactPubkey = mainProfile.pubkey)) " +
                "AS isFollowedByMe, " +
                // SELECT numOfFollowing
                "(SELECT COUNT(contactPubkey) FROM contact WHERE pubkey = :pubkey) AS numOfFollowing, " +
                // SELECT numOfFollowers
                "(SELECT COUNT(pubkey) FROM contact WHERE contactPubkey = :pubkey) AS numOfFollowers " +
                "FROM profile AS mainProfile " +
                "WHERE mainProfile.pubkey = :pubkey"
    )
    fun getProfileEntityExtendedFlow(
        pubkey: String,
        personalPubkey: String
    ): Flow<ProfileEntityExtended?>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM profile WHERE pubkey = :pubkey")
    fun getMetadata(pubkey: String): Flow<Metadata?>

    @Query(
        "UPDATE profile " +
                "SET name = :name, " +
                "about = :about, " +
                "picture = :picture, " +
                "nip05 = :nip05, " +
                "lud16 = :lud16 " +
                "WHERE pubkey = :pubkey"
    )
    suspend fun updateMetadata(
        pubkey: String,
        name: String,
        about: String,
        picture: String,
        nip05: String,
        lud16: String,
    )

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg profile: ProfileEntity)

    @Query(
        "DELETE FROM profile " +
                "WHERE pubkey = :pubkey AND createdAt < :newTimestamp"
    )
    suspend fun deleteIfOutdated(pubkey: String, newTimestamp: Long)

    @Transaction
    suspend fun insertAndDeleteOutdated(
        pubkey: String,
        newTimestamp: Long,
        profile: ProfileEntity
    ) {
        deleteIfOutdated(pubkey = pubkey, newTimestamp = newTimestamp)
        insertOrIgnore(profile)
    }

    @Query(
        "DELETE FROM profile " +
                "WHERE pubkey NOT IN (SELECT pubkey FROM post) " +
                "AND pubkey NOT IN (:exclude)"
    )
    suspend fun deleteOrphaned(exclude: Collection<String>): Int


    @MapInfo(keyColumn = "pubkey", valueColumn = "name")
    @Query("SELECT pubkey, name FROM profile WHERE pubkey IN (:pubkeys)")
    fun getPubkeyToNameMapFlow(pubkeys: Collection<String>): Flow<Map<String, String>>
}
