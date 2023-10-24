package com.dluvian.nozzle.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.dluvian.nozzle.data.room.entity.AccountEntity
import com.dluvian.nozzle.model.Pubkey

@Dao
interface AccountDao {

    @Transaction
    suspend fun setAccounts(pubkeys: List<Pubkey>) {
        if (pubkeys.isEmpty()) return
        val accounts = pubkeys.mapIndexed { i, key ->
            AccountEntity(pubkey = key, isActive = i == 0)
        }
        deleteAll()
        insert(*accounts.toTypedArray())
    }

    @Transaction
    suspend fun activateAccount(pubkey: Pubkey) {
        activateSinglePubkey(pubkey = pubkey)
        deactivateAllExcept(excludePubkey = pubkey)
    }

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(vararg accounts: AccountEntity): List<Long>

    @Query("UPDATE account SET isActive = 0 WHERE pubkey IS NOT :excludePubkey")
    suspend fun deactivateAllExcept(excludePubkey: Pubkey)

    @Query("UPDATE account SET isActive = 1 WHERE pubkey = :pubkey")
    suspend fun activateSinglePubkey(pubkey: Pubkey)

    @Query("DELETE FROM account")
    suspend fun deleteAll()
}
