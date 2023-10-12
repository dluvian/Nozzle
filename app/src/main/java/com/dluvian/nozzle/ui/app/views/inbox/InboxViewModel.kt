package com.dluvian.nozzle.ui.app.views.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class InboxViewModel : ViewModel() {
    companion object {
        fun provideFactory(
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return InboxViewModel(
                    ) as T
                }
            }
    }
}
