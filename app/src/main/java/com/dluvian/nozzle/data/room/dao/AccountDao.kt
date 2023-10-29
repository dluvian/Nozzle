package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.nozzle.data.room.entity.AccountEntity
import com.dluvian.nozzle.data.room.helper.extended.AccountEntityExtended
import com.dluvian.nozzle.model.Pubkey
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    @Query(
        "SELECT account.pubkey, account.isActive, profile.name, profile.picture " +
                "FROM account " +
                "LEFT JOIN profile ON account.pubkey = profile.pubkey"
    )
    fun listAccountsFlow(): Flow<List<AccountEntityExtended>>

    @Transaction
    suspend fun setAccounts(pubkeys: List<Pubkey>, activeIndex: Int) {
        if (pubkeys.isEmpty()) return
        val accounts = pubkeys.mapIndexed { i, key ->
            AccountEntity(pubkey = key, isActive = i == activeIndex)
        }
        deleteAllAccounts()
        insertAccount(*accounts.toTypedArray())
    }

    @Transaction
    suspend fun activateAccount(pubkey: Pubkey) {
        activateSinglePubkey(pubkey = pubkey)
        deactivateAllExcept(excludePubkey = pubkey)
    }

    @Query("DELETE FROM account WHERE pubkey = :pubkey")
    suspend fun deleteAccount(pubkey: Pubkey)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAccount(vararg accounts: AccountEntity): List<Long>

    @Query("UPDATE account SET isActive = 0 WHERE pubkey IS NOT :excludePubkey")
    suspend fun deactivateAllExcept(excludePubkey: Pubkey)

    @Query("UPDATE account SET isActive = 1 WHERE pubkey = :pubkey")
    suspend fun activateSinglePubkey(pubkey: Pubkey)

    @Query("DELETE FROM account")
    suspend fun deleteAllAccounts()
}
