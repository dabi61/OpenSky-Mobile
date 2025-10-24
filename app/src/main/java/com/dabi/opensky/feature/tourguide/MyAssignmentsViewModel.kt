// feature/tourguide/MyAssignmentsViewModel.kt
package com.dabi.opensky.feature.tourguide

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.tourguide.TourGuideRepository
import com.dabi.opensky.core.model.tourguide.MyAssignmentsResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyAssignmentsViewModel @Inject constructor(
    private val repo: TourGuideRepository
) : ViewModel() {

    private val _ui = MutableStateFlow<Resource<MyAssignmentsResponse>>(Resource.Loading)
    val ui: StateFlow<Resource<MyAssignmentsResponse>> = _ui.asStateFlow()

    fun load(page: Int = 1, size: Int = 10) {
        viewModelScope.launch {
            _ui.value = Resource.Loading
            _ui.value = repo.getMyAssignments(page, size)
        }
    }
}
