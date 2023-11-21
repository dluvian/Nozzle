package com.dluvian.nozzle.ui.app.views.likes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.dluvian.nozzle.data.SCOPE_TIMEOUT
import com.dluvian.nozzle.data.cache.IClickedMediaUrlCache
import com.dluvian.nozzle.model.PostWithMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LikesViewModel(
    val clickedMediaUrlCache: IClickedMediaUrlCache
) : ViewModel() {
    private val _uiState = MutableStateFlow("LOL")
    val uiState = _uiState.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(stopTimeoutMillis = SCOPE_TIMEOUT),
        _uiState.value
    )

    var likedPosts: StateFlow<List<PostWithMeta>> = MutableStateFlow(emptyList())

    val onOpenLikes: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            // TODO
        }
    }

    val onRefresh: () -> Unit = {
        viewModelScope.launch(context = Dispatchers.IO) {
            // TODO
        }
    }

    val onLoadMore: () -> Unit = local@{
        // TODO
    }


    companion object {
        fun provideFactory(
            clickedMediaUrlCache: IClickedMediaUrlCache,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return LikesViewModel(
                        clickedMediaUrlCache = clickedMediaUrlCache
                    ) as T
                }
            }
    }
}
