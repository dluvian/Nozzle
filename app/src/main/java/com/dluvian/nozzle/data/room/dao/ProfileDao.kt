package com.dluvian.nozzle.data.room.dao

import androidx.room.*
import com.dluvian.nozzle.data.room.entity.ProfileEntity
import com.dluvian.nozzle.data.room.helper.extended.ProfileEntityExtended
import com.dluvian.nozzle.data.utils.NORMAL_DEBOUNCE
import com.dluvian.nozzle.data.utils.firstThenDistinctDebounce
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.SimpleProfile
import com.dluvian.nozzle.model.nostr.Metadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profile WHERE pubkey = :pubkey")
    suspend fun getProfile(pubkey: String): ProfileEntity?

    @Query("SELECT * FROM profile WHERE pubkey IN (:pubkeys)")
    fun listProfiles(pubkeys: Collection<Pubkey>): Flow<List<ProfileEntity>>

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

    fun getSimpleProfilesFlow(
        pubkeys: Collection<Pubkey>,
        contactDao: ContactDao,
        myPubkey: String,
    ): Flow<List<SimpleProfile>> {
        val baseFlow = flowOf(pubkeys.toSet())
        val profileFlow = listProfiles(pubkeys = pubkeys)
            .firstThenDistinctDebounce(NORMAL_DEBOUNCE)
        val trustScoreFlow = contactDao.getTrustScoreByPubkeyFlow(contactPubkeys = pubkeys)
        val myFollowerListFlow = contactDao.listPersonalContactPubkeysFlow()

        return combine(
            baseFlow,
            profileFlow,
            trustScoreFlow,
            myFollowerListFlow
        ) { baseList, profiles, score, followers ->
            baseList.map { pubkey ->
                val profile = profiles.find { it.pubkey == pubkey }
                SimpleProfile(
                    name = profile?.metadata?.name.orEmpty(),
                    picture = profile?.metadata?.picture.orEmpty(),
                    pubkey = pubkey,
                    trustScore = score[pubkey] ?: 0f,
                    isOneself = pubkey == myPubkey,
                    isFollowedByMe = followers.contains(pubkey)
                )
            }
        }
    }
}
