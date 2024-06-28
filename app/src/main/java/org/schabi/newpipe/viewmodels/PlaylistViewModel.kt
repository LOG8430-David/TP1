package org.schabi.newpipe.viewmodels

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.playlist.PlaylistInfo
import org.schabi.newpipe.paging.PlaylistItemsSource
import org.schabi.newpipe.util.KEY_SERVICE_ID
import org.schabi.newpipe.util.KEY_URL
import org.schabi.newpipe.util.NO_SERVICE_ID

class PlaylistViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val serviceIdState = savedStateHandle.getStateFlow(KEY_SERVICE_ID, NO_SERVICE_ID)
    private val urlState = savedStateHandle.getStateFlow(KEY_URL, "")

    val playlistInfo = serviceIdState.combine(urlState) { id, url ->
        PlaylistInfo.getInfo(NewPipe.getService(id), url)
    }
        .flowOn(Dispatchers.IO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val streamItems = playlistInfo
        .filterNotNull()
        .flatMapLatest {
            Pager(PagingConfig(pageSize = 20, enablePlaceholders = false)) {
                PlaylistItemsSource(it)
            }.flow
        }
        .cachedIn(viewModelScope)
}
