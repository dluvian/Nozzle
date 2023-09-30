package com.dluvian.nozzle.ui.app.views.hashtag

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dluvian.nozzle.data.utils.*
import kotlinx.coroutines.flow.*

private const val TAG = "HashtagViewModel"

class HashtagViewModel : ViewModel() {
    companion object {
        fun provideFactory(): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return HashtagViewModel() as T
            }
        }
    }
}
