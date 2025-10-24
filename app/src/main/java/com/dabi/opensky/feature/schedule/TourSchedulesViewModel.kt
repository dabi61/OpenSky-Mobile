package com.dabi.opensky.feature.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.schedule.ScheduleRepository
import com.dabi.opensky.core.model.schedule.ScheduleItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TourSchedulesViewModel @Inject constructor(
    private val repo: ScheduleRepository
) : ViewModel() {

    data class UiState(
        val isLoading: Boolean = false,
        val schedules: List<ScheduleItem> = emptyList(),
        val page: Int = 1,
        val size: Int = 10,
        val totalPages: Int = 1,
        val error: Throwable? = null
    )

    private val _ui = MutableStateFlow(UiState())
    val ui = _ui.asStateFlow()

    private var currentTourId: String? = null
    private var isLoadingMore = false

    fun init(tourId: String, page: Int = 1, size: Int = 10) {
        if (currentTourId == tourId && _ui.value.schedules.isNotEmpty()) return
        currentTourId = tourId
        _ui.value = UiState(isLoading = true, page = page, size = size)
        viewModelScope.launch {
            when (val res = repo.getSchedulesByTour(tourId, page, size)) {
                is Resource.Success -> {
                    _ui.value = _ui.value.copy(
                        isLoading = false,
                        schedules = res.data.schedules,
                        totalPages = res.data.totalPages,
                        error = null
                    )
                }
                is Resource.Error -> _ui.value = _ui.value.copy(
                    isLoading = false,
                    error = res.cause
                )
                is Resource.Loading -> Unit
            }
        }
    }

    fun loadNextPage() {
        val tourId = currentTourId ?: return
        val st = _ui.value
        if (isLoadingMore || st.page >= st.totalPages) return

        isLoadingMore = true
        val next = st.page + 1
        viewModelScope.launch {
            when (val res = repo.getSchedulesByTour(tourId, next, st.size)) {
                is Resource.Success -> {
                    _ui.value = st.copy(
                        schedules = st.schedules + res.data.schedules,
                        page = next,
                        totalPages = res.data.totalPages,
                        error = null
                    )
                }
                is Resource.Error -> _ui.value = st.copy(error = res.cause)
                else -> Unit
            }
            isLoadingMore = false
        }
    }

    fun retry() {
        currentTourId?.let { init(it, _ui.value.page, _ui.value.size) }
    }
}