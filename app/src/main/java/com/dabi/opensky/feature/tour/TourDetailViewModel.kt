package com.dabi.opensky.feature.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.tour.TourRepository
import com.dabi.opensky.core.model.tour.TourDetailResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TourDetailUiState(
    val detail: Resource<TourDetailResponse> = Resource.Loading
)

@HiltViewModel
class TourDetailViewModel @Inject constructor(
    private val repo: TourRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(TourDetailUiState())
    val ui: StateFlow<TourDetailUiState> = _ui

    fun load(id: String) {
        viewModelScope.launch {
            _ui.update { it.copy(detail = Resource.Loading) }
            repo.getTourDetail(id).collect { res -> _ui.update { it.copy(detail = res) } }
        }
    }
}
