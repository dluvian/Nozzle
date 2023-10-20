package com.dluvian.nozzle.ui.app.views.addAccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.nostr.utils.EncodingUtils
import com.dluvian.nozzle.data.nostr.utils.KeyUtils
import com.dluvian.nozzle.data.room.dao.AccountDao
import com.dluvian.nozzle.data.room.entity.AccountEntity
import com.dluvian.nozzle.data.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class AddAccountViewModel(
    accountDao: AccountDao,
) : ViewModel() {

    val onOpenScreen: () -> Unit = {
//        _uiState.update{ it.copy(isInvalid = false) }
    }

    val onAddAccount: (String) -> Unit = local@{ pubkey ->
        if (!KeyUtils.isValidPubkey(pubkey)) {
//            _uiState.update{ it.copy(isInvalid = true) }
            return@local
        }
//            _uiState.update{ it.copy(isLoading = true) }
        viewModelScope.launch(context = Dispatchers.IO) {
            val account = AccountEntity(pubkey = pubkey)
            val insertCode = accountDao.insertIfNotPresent(account)
//            if (insertCode == -1) {
//              _uiState.update{ it.copy(pubkeyAlreadyExists = true) }
//            }
            // if(uiState.value.isGenerated){
            //      insertAndPublishDefaultRelays
            // }
        }.invokeOnCompletion {
//            _uiState.update{ it.copy(isLoading = false)
        }
    }

    val onGeneratePubkey: () -> Unit = {
        val privkey = KeyUtils.generatePrivkey()
        val pubkey = KeyUtils.derivePubkey(privkey = privkey)
        val npub = EncodingUtils.hexToNpub(pubkey)
    }

    companion object {
        fun provideFactory(
            accountDao: AccountDao,
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddAccountViewModel(
                    accountDao = accountDao
                ) as T
            }
        }
    }
}
