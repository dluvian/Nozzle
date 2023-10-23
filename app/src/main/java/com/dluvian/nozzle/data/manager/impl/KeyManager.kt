package com.dluvian.nozzle.data.manager.impl

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dluvian.nozzle.data.PreferenceFileNames
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.hexToNpub
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils.hexToNsec
import com.dluvian.nozzle.data.nostr.utils.KeyUtils.derivePubkey
import com.dluvian.nozzle.data.nostr.utils.KeyUtils.generatePrivkey
import com.dluvian.nozzle.data.nostr.utils.KeyUtils.isValidHexKey
import com.dluvian.nozzle.data.room.dao.AccountDao
import com.dluvian.nozzle.data.room.entity.AccountEntity
import com.dluvian.nozzle.model.Pubkey
import com.dluvian.nozzle.model.helper.PubkeyVariations
import com.dluvian.nozzle.model.nostr.Keys
import fr.acinq.secp256k1.Hex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


private const val TAG: String = "KeyManager"
private const val PRIVKEY: String = "privkey"
private const val DELIMITER: String = ";"

class KeyManager(context: Context, private val accountDao: AccountDao) : IKeyManager {
    private val activePubkeyFlow = accountDao.getActivePubkeyFlow()
        .distinctUntilChanged()
        .map { key ->
            Log.w("LOLOL", "LOLOL")
            key?.let { PubkeyVariations(pubkey = it, npub = hexToNpub(it), shortenedNpub = "") }
        }
        .stateIn(CoroutineScope(Dispatchers.IO), SharingStarted.Eagerly, null)

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val preferences = EncryptedSharedPreferences.create(
        context,
        PreferenceFileNames.KEYS,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    init {
        val privkeys = getPrivkeys().ifEmpty { listOf(generatePrivkey()) }
        setPrivkeys(privkeys)

        // Do this always for migration
        CoroutineScope(Dispatchers.Main).launch {
            val pubkeys = privkeys.map { derivePubkey(it) }
            accountDao.setAccounts(pubkeys = pubkeys)
        }
    }

    override fun getActivePubkey() = activePubkeyFlow.value?.pubkey ?: throw IllegalStateException()

    override fun getActiveNpub() = activePubkeyFlow.value?.npub ?: throw IllegalStateException()

    override fun getActivePrivkey() = getPrivkeys().first()

    override fun getActiveNsec() = hexToNsec(getActivePrivkey())

    override suspend fun activatePubkey(pubkey: Pubkey) {
        if (!isValidHexKey(pubkey) || getActivePubkey() == pubkey) return

        val privkeys = getPrivkeys()
        val pubkeys = privkeys.map { derivePubkey(it) }
        val index = pubkeys.indexOf(pubkey)
        if (index == -1) {
            Log.w(TAG, "Pubkey $pubkey not found in derived privkey list (n=${privkeys.size}")
            return
        }

        val reorderedPrivkeys = mutableListOf<String>()
        reorderedPrivkeys.add(privkeys[index])
        privkeys.forEachIndexed { i, key -> if (i != index) reorderedPrivkeys.add(key) }

        setPrivkeys(privkeys = reorderedPrivkeys)
        accountDao.activateAccount(pubkey)
        return
    }

    override suspend fun addPrivkey(privkey: String) {
        if (!isValidHexKey(privkey)) return

        val privkeys = getPrivkeys()
        if (privkeys.contains(privkey)) return

        val pubkey = derivePubkey(privkey)
        val newAccount = AccountEntity(pubkey = pubkey, isActive = false)
        setPrivkeys(privkeys = privkeys + privkey)
        accountDao.insert(newAccount)
    }

    override suspend fun deletePubkey(pubkey: Pubkey) {
        val privkeys = getPrivkeys()
        val pubkeys = privkeys.map { derivePubkey(it) }
        val index = pubkeys.indexOf(pubkey)

        val newPrivkeys = privkeys.filterIndexed { i, _ -> i != index }
        setPrivkeys(privkeys = newPrivkeys)
    }

    override fun getActiveKeys(): Keys {
        return Keys(
            privkey = Hex.decode(getActivePrivkey()),
            pubkey = Hex.decode(getActivePubkey())
        )
    }

    private fun getPrivkeys(): List<String> {
        return preferences.getString(PRIVKEY, "")?.split(DELIMITER) ?: emptyList()
    }

    private fun setPrivkeys(privkeys: List<String>) {
        val combinedString = privkeys.joinToString(separator = DELIMITER)
        preferences.edit()
            .putString(PRIVKEY, combinedString)
            .apply()
    }
}
