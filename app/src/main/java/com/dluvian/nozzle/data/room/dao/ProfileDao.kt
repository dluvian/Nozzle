package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.room.helper.extended.ProfileEntityExtended
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE pubkey = :pubkey")
    suspend fun getProfile(pubkey: String): ProfileEntity?

    @Query(
        // SELECT metadata
        "SELECT mainProfile.*, " +
                // SELECT numOfFollowing
                "(SELECT COUNT(contactPubkey) FROM contact WHERE pubkey = :pubkey) AS numOfFollowing, " +
                // SELECT numOfFollowers
                "(SELECT COUNT(pubkey) FROM contact WHERE contactPubkey = :pubkey) AS numOfFollowers " +
                "FROM profile AS mainProfile " +
                "WHERE mainProfile.pubkey = :pubkey"
    )
    fun getProfileEntityExtendedFlow(pubkey: String): Flow<ProfileEntityExtended?>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM profile WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)")
    fun getActiveMetadata(): Flow<Metadata?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(vararg profile: ProfileEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(vararg profile: ProfileEntity)

    @Query("DELETE FROM profile WHERE pubkey IN (:pubkeys)")
    suspend fun delete(pubkeys: Collection<String>)

    @Query(
        "SELECT pubkey, createdAt " +
                "FROM profile " +
                "WHERE pubkey IN (:pubkeys)" +
                "GROUP BY pubkey"
    )
    suspend fun getTimestampByPubkey(pubkeys: Collection<String>): Map<
            @MapColumn("pubkey") Pubkey,
            @MapColumn("createdAt") Long
            >

    @Transaction
    suspend fun insertAndDeleteOutdated(profiles: Collection<ProfileEntity>) {
        if (profiles.isEmpty()) return

        val timestamps = getTimestampByPubkey(pubkeys = profiles.map(ProfileEntity::pubkey))
        val outdatedPubkeys = profiles
            .filter { it.createdAt < (timestamps[it.pubkey] ?: 0L) }
            .map { it.pubkey }

        if (outdatedPubkeys.isNotEmpty()) delete(pubkeys = outdatedPubkeys)

        insertOrIgnore(*profiles.toTypedArray())
    }

    @Query(
        "DELETE FROM profile " +
                "WHERE pubkey NOT IN (SELECT pubkey FROM post) " +
                "AND pubkey NOT IN (:exclude)" +
                "AND pubkey NOT IN (SELECT pubkey FROM account) " +
                "AND pubkey NOT IN (SELECT contactPubkey FROM contact WHERE pubkey IN (SELECT pubkey FROM account))"
    )
    suspend fun deleteOrphaned(exclude: Collection<String>): Int

    @MapInfo(keyColumn = "pubkey", valueColumn = "name")
    @Query("SELECT pubkey, name FROM profile WHERE pubkey IN (:pubkeys)")
    fun getPubkeyToNameMapFlow(pubkeys: Collection<String>): Flow<Map<String, String>>

    @Query(
        "SELECT pubkey " +
                "FROM profile " +
                "WHERE pubkey IN (:pubkeys)"
    )
    suspend fun filterExistingPubkeys(pubkeys: Collection<String>): List<String>
}
