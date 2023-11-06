package com.dluvian.nozzle.data.manager.impl

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dluvian.nozzle.data.PreferenceFileNames
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.hexToNsec
import com.dluvian.nozzle.data.nostr.utils.KeyUtils.derivePubkey
import com.dluvian.nozzle.data.nostr.utils.KeyUtils.isValidHexKey
import com.dluvian.nozzle.data.room.dao.AccountDao
import com.dluvian.nozzle.data.room.entity.AccountEntity
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.nostr.Keys
import fr.acinq.secp256k1.Hex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


private const val TAG: String = "KeyManager"
private const val PRIVKEY: String = "privkey"
private const val ACTIVE_INDEX: String = "active_index"
private const val DELIMITER: String = ";"

class KeyManager(context: Context, private val accountDao: AccountDao) : IKeyManager {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // TODO: Save encrypted in room. No more sharedRef
    private val preferences = EncryptedSharedPreferences.create(
        context,
        PreferenceFileNames.KEYS,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // TODO: MutableState is enough? Why FLow?
    private val _currentPubkey = MutableStateFlow("")
    private val currentPubkey = _currentPubkey.stateIn(
        scope, SharingStarted.Eagerly, getActivePubkey()
    )

    override val hasPrivkey = mutableStateOf(false)

    init {
        val privkeys = getPrivkeys().toMutableList()
        if (privkeys.isNotEmpty()) {
            hasPrivkey.value = true
            val activeIndex = getActiveIndex()
            val pubkey = derivePubkey(privkeys[activeIndex])
            _currentPubkey.update { pubkey }
            // Ensure data integrity in database
            scope.launch {
                val pubkeys = privkeys.map { derivePubkey(it) }
                accountDao.setAccounts(pubkeys = pubkeys, activeIndex = activeIndex)
            }
        }
    }

    override fun getActivePubkey() = _currentPubkey.value

    override fun getActiveNsec() = hexToNsec(getActivePrivkey())

    override suspend fun activatePubkey(pubkey: Pubkey) {
        if (!isValidHexKey(pubkey) || getActivePubkey() == pubkey) return

        val pubkeys = getPrivkeys().map { derivePubkey(it) }
        val indexToActivate = pubkeys.indexOf(pubkey)
        if (indexToActivate == -1) {
            Log.w(TAG, "Pubkey $pubkey not found in derived privkey list")
            return
        }

        setActiveIndex(indexToActivate)
        accountDao.activateAccount(pubkey)
        _currentPubkey.update { pubkey }
        return
    }

    override suspend fun addPrivkey(privkey: String) {
        if (!isValidHexKey(privkey)) return

        val privkeys = getPrivkeys()
        if (privkeys.contains(privkey)) return

        val pubkey = derivePubkey(privkey)
        val newAccount = AccountEntity(pubkey = pubkey, isActive = false)
        setPrivkeys(privkeys = privkeys + privkey)
        accountDao.insertAccount(newAccount)
    }

    override suspend fun deletePubkey(pubkey: Pubkey) {
        val privkeys = getPrivkeys()
        val pubkeys = privkeys.map { derivePubkey(it) }
        val indexToDelete = pubkeys.indexOf(pubkey)
        if (indexToDelete == -1) {
            Log.w(TAG, "Pubkey $pubkey not found in derived privkey list (n=${privkeys.size}")
            return
        }
        val oldActiveIndex = getActiveIndex()
        if (oldActiveIndex == indexToDelete) {
            Log.w(TAG, "Attempt to delete active account")
            return
        }

        if (oldActiveIndex > indexToDelete) {
            setActiveIndex(oldActiveIndex - 1)
        }

        val newPrivkeys = privkeys.filterIndexed { i, _ -> i != indexToDelete }
        setPrivkeys(privkeys = newPrivkeys)
        accountDao.deleteAccount(pubkey = pubkey)
    }

    override fun getActiveKeys(): Keys {
        return Keys(
            privkey = Hex.decode(getActivePrivkey()),
            pubkey = Hex.decode(getActivePubkey())
        )
    }

    override fun getActivePubkeyStateFlow(): StateFlow<String> = currentPubkey

    private fun getActivePrivkey() = getPrivkeys()[getActiveIndex()]

    private fun getPrivkeys(): List<String> {
        val saved = preferences.getString(PRIVKEY, "")?.split(DELIMITER) ?: emptyList()
        return saved.filter(String::isNotEmpty)
    }

    private fun getActiveIndex(): Int {
        return preferences.getInt(ACTIVE_INDEX, 0)
    }

    private fun setPrivkeys(privkeys: List<String>) {
        hasPrivkey.value = privkeys.isNotEmpty()
        val combinedString = privkeys.joinToString(separator = DELIMITER)
        preferences.edit()
            .putString(PRIVKEY, combinedString)
            .apply()
    }

    private fun setActiveIndex(index: Int) {
        preferences.edit()
            .putInt(ACTIVE_INDEX, index)
            .apply()
    }
}
