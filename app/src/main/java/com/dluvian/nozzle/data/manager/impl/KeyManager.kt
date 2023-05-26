package com.dluvian.nozzle.data.manager.impl

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.dluvian.nostrclientkt.model.Keys
import com.dluvian.nozzle.data.PreferenceFileNames
import com.dluvian.nozzle.data.manager.IKeyManager
import com.dluvian.nozzle.data.utils.*
import fr.acinq.secp256k1.Hex


private const val TAG: String = "KeyManager"

private const val PRIVKEY: String = "privkey"


class KeyManager(context: Context) : IKeyManager {
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
    private var pubkey: String = ""
    private var npub: String = ""

    init {
        Log.i(TAG, "Initialize KeyPreferences")
        var privkey = getPrivkey()
        if (privkey.isEmpty()) {
            privkey = generatePrivkey()
            Log.i(TAG, "Setting initial privkey $privkey")
            setPrivkey(privkey)
        }
        setPubkeyAndNpub(privkey)
    }

    override fun getPubkey() = pubkey

    override fun getNpub() = hexToNpub(pubkey)

    override fun getPrivkey() = preferences.getString(PRIVKEY, "") ?: ""

    override fun getNsec() = hexToNsec(getPrivkey())

    override fun setPrivkey(privkey: String) {
        val hex = if (privkey.startsWith("nsec1"))
            nsecToHex(privkey).getOrThrow() else privkey
        setPubkeyAndNpub(hex)
        Log.i(TAG, "Setting privkey and derived pubkey $pubkey")
        preferences.edit()
            .putString(PRIVKEY, hex)
            .apply()
    }

    override fun getKeys(): Keys {
        return Keys(
            privkey = Hex.decode(getPrivkey()),
            pubkey = Hex.decode(getPubkey())
        )
    }

    private fun setPubkeyAndNpub(privkey: String) {
        pubkey = derivePubkey(privkey)
        npub = hexToNpub(pubkey)
    }

}
