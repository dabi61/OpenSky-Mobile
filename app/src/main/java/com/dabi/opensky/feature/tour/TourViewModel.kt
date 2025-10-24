package com.dabi.opensky.feature.tour

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.tour.TourRepository
import com.dabi.opensky.core.model.tour.Tour
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ToursUiState(
    val items: List<Tour> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val page: Int = 1,
    val size: Int = 10,
    val endReached: Boolean = false
) {
    val isEmpty: Boolean get() = !isLoading && error == null && items.isEmpty()
}

@HiltViewModel
class ToursViewModel @Inject constructor(
    private val repo: TourRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(ToursUiState())
    val ui: StateFlow<ToursUiState> = _ui

    fun start(size: Int = 10) {
        if (_ui.value.items.isNotEmpty()) return
        _ui.update { it.copy(page = 1, size = size) }
        loadPage(1)
    }

    fun refresh() {
        _ui.update { ToursUiState(size = it.size) }
        loadPage(1)
    }

    fun loadNextPage() {
        val s = _ui.value
        if (s.isLoading || s.endReached) return
        loadPage(s.page + 1)
    }

    private fun loadPage(page: Int) {
        val size = _ui.value.size
        viewModelScope.launch {
            _ui.update { it.copy(isLoading = true, error = null) }
            repo.getTours(page, size).collect { res ->
                when (res) {
                    is Resource.Loading -> _ui.update { it.copy(isLoading = true) }
                    is Resource.Error   -> _ui.update { it.copy(isLoading = false, error = res.cause.message) }
                    is Resource.Success -> {
                        val (tours, meta) = res.data
                        _ui.update {
                            it.copy(
                                isLoading = false,
                                items = if (page == 1) tours else it.items + tours,
                                page = meta.currentPage,
                                endReached = !meta.hasNextPage
                            )
                        }
                    }
                }
            }
        }
    }
}
