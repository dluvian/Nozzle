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
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfNotPresent(account: AccountEntity): Long

    @Transaction
    suspend fun activateAccount(pubkey: Pubkey) {
        activate(pubkey = pubkey)
        deactivateAllExcept(excludePubkey = pubkey)
    }

    @Query("UPDATE account SET isActive = 0 WHERE pubkey IS NOT :excludePubkey")
    suspend fun deactivateAllExcept(excludePubkey: Pubkey)

    @Query("UPDATE account SET isActive = 1 WHERE pubkey = :pubkey")
    suspend fun activate(pubkey: Pubkey)
}
