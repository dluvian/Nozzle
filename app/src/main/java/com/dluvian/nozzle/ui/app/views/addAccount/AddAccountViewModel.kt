package com.dluvian.nozzle.ui.app.views.addAccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dluvian.nozzle.data.utils.*
import kotlinx.coroutines.flow.*


class AddAccountViewModel : ViewModel() {
    companion object {
        fun provideFactory(
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AddAccountViewModel(
                ) as T
            }
        }
    }
}
