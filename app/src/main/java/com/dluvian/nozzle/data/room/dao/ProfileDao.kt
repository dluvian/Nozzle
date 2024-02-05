package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.MapColumn
import androidx.room.MapInfo
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RewriteQueriesToDropUnusedColumns
import androidx.room.Transaction
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.utils.escapeSQLPercentChars
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE pubkey = :pubkey")
    suspend fun getProfile(pubkey: String): ProfileEntity?

    @Query("SELECT * FROM profile WHERE pubkey = :pubkey")
    fun getProfileFlow(pubkey: String): Flow<ProfileEntity?>

    @Query("SELECT * FROM profile WHERE pubkey IN (:pubkeys)")
    fun getProfilesFlow(pubkeys: Collection<Pubkey>): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profile WHERE pubkey IN (:pubkeys)")
    suspend fun getProfiles(pubkeys: Collection<Pubkey>): List<ProfileEntity>

    @RewriteQueriesToDropUnusedColumns
    @Query("SELECT * FROM profile WHERE pubkey = (SELECT pubkey FROM account WHERE isActive = 1)")
    fun getActiveMetadata(): Flow<Metadata?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfiles(vararg profile: ProfileEntity)

    @Query(
        "SELECT pubkey, MAX(createdAt) as maxCreatedAt " +
                "FROM profile " +
                "WHERE pubkey IN (:pubkeys)" +
                "GROUP BY pubkey"
    )
    suspend fun getTimestampByPubkey(pubkeys: Collection<String>): Map<
            @MapColumn("pubkey") Pubkey,
            @MapColumn("maxCreatedAt") Long
            >

    @Transaction
    suspend fun insertAndReplaceOutdated(profiles: Collection<ProfileEntity>) {
        if (profiles.isEmpty()) return

        val timestamps = getTimestampByPubkey(pubkeys = profiles.map(ProfileEntity::pubkey))
        val toInsert = profiles.filter { new -> new.createdAt > (timestamps[new.pubkey] ?: 0L) }

        if (toInsert.isEmpty()) return

        upsertProfiles(*toInsert.toTypedArray())
    }

    @Query(
        "DELETE FROM profile " +
                "WHERE pubkey NOT IN (SELECT pubkey FROM post) " +
                "AND pubkey NOT IN (:exclude)" +
                "AND pubkey NOT IN (SELECT pubkey FROM account) " +
                "AND pubkey NOT IN (SELECT contactPubkey FROM contact WHERE pubkey IN (SELECT pubkey FROM account)) " +
                "AND pubkey NOT IN (SELECT pubkey FROM mention)"
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

    // UNION ALL retains order
    @Query(
        "SELECT pubkey FROM profile WHERE name = :name " +
                "UNION ALL " +
                "SELECT pubkey FROM profile WHERE name LIKE :start ESCAPE '\\' " +
                "UNION ALL " +
                "SELECT pubkey FROM profile WHERE nip05 LIKE :start ESCAPE '\\' " +
                "UNION ALL " +
                "SELECT pubkey FROM profile WHERE name LIKE :somewhere ESCAPE '\\' " +
                "UNION ALL " +
                "SELECT pubkey FROM profile WHERE nip05 LIKE :somewhere ESCAPE '\\' " +
                "UNION ALL " +
                "SELECT pubkey FROM profile WHERE about LIKE :start ESCAPE '\\' " +
                "UNION ALL " +
                "SELECT pubkey FROM profile WHERE about LIKE :somewhere ESCAPE '\\' " +
                "LIMIT :limit"
    )
    suspend fun internalGetPubkeysWithNameLike(
        name: String,
        start: String,
        somewhere: String,
        limit: Int
    ): List<Pubkey>

    suspend fun getPubkeysWithNameLike(name: String, limit: Int): List<Pubkey> {
        val fixedName = name.escapeSQLPercentChars()
        return internalGetPubkeysWithNameLike(
            name = fixedName,
            start = "$fixedName%",
            somewhere = "%$fixedName%",
            limit = limit
        )
    }
}
