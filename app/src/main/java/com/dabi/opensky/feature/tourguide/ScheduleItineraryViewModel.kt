// feature/tourguide/ScheduleItineraryViewModel.kt
package com.dabi.opensky.feature.tourguide

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.repository.tourguide.TourGuideRepository
import com.dabi.opensky.core.model.tourguide.*
import com.dabi.opensky.feature.tour.nowUtcZMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleItineraryViewModel @Inject constructor(
    private val repo: TourGuideRepository
) : ViewModel() {

    data class RuntimeState(
        val scheduleItID: String? = null,
        val startTime: String? = null,
        val endTime: String? = null
    )

    data class Ui(
        val list: Resource<List<TourItinerary>> = Resource.Loading,
        val isActing: Boolean = false,
        val actMessage: String? = null,
        val error: Throwable? = null,
        // key = itineraryID
        val rt: Map<String, RuntimeState> = emptyMap(),
        val scheduleId: String = "",
        val tourId: String = ""
    )

    private val _ui = MutableStateFlow(Ui())
    val ui: StateFlow<Ui> = _ui.asStateFlow()

    fun init(scheduleId: String, tourId: String) {
        _ui.update { it.copy(scheduleId = scheduleId, tourId = tourId) }
        loadCombined(scheduleId, tourId)
    }

    fun refresh() = loadCombined(_ui.value.scheduleId, _ui.value.tourId)

    private fun loadCombined(scheduleId: String, tourId: String) {
        viewModelScope.launch {
            _ui.update { it.copy(list = Resource.Loading, error = null) }

            // chạy song song 2 call
            val tourDef = async { repo.getTourItinerariesByTour(tourId) }
            val schedDef = async { repo.getScheduleItinerariesBySchedule(scheduleId) }

            val tourRes = tourDef.await()
            val schedRes = schedDef.await()

            // Cập nhật list (ưu tiên báo lỗi nếu list tour lỗi)
            when (tourRes) {
                is Resource.Success -> _ui.update { it.copy(list = tourRes) }
                is Resource.Error   -> { _ui.update { it.copy(list = tourRes) }; return@launch }
                is Resource.Loading -> {}
            }

            // Build rt map từ schedule_itinerary
            val rtMap: Map<String, RuntimeState> = when (schedRes) {
                is Resource.Success -> {
                    schedRes.data.associateBy(
                        keySelector = { it.itineraryID },
                        valueTransform = { RuntimeState(it.scheduleItID, it.startTime, it.endTime) }
                    )
                }
                else -> emptyMap()
            }

            _ui.update { it.copy(rt = rtMap) }

            // Nếu có lỗi từ schedRes, bắn error nhẹ (không chặn list)
            if (schedRes is Resource.Error) {
                _ui.update { it.copy(error = schedRes.cause) }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun start(itineraryId: String, nowIso: String) {
        val scheduleId = ui.value.scheduleId
        val startTimeIso = nowUtcZMillis() // ✅ tạo ISO-Z đúng docs

        // Nếu đã có start → không gọi lại (tránh lỗi unique/500)
        ui.value.rt[itineraryId]?.startTime?.let {
            _ui.update { it.copy(actMessage = "Điểm dừng này đã được bắt đầu trước đó.") }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(isActing = true, actMessage = null, error = null) }
            when (val res = repo.startItinerary(
                StartItineraryRequest(scheduleID = scheduleId, itineraryID = itineraryId, startTime = startTimeIso)
            )) {
                is Resource.Success -> {
                    val sid = res.data.scheduleItId
                    _ui.update {
                        it.copy(
                            isActing = false,
                            actMessage = res.data.message,
                            rt = it.rt.toMutableMap().apply {
                                this[itineraryId] = RuntimeState(
                                    scheduleItID = sid,
                                    startTime = startTimeIso,
                                    endTime = null
                                )
                            }
                        )
                    }
                }
                is Resource.Error -> _ui.update { it.copy(isActing = false, error = res.cause) }
                else -> {}
            }
        }
    }

    /** Convenience: tự sinh endTime ISO-Z */
    @RequiresApi(Build.VERSION_CODES.O)
    fun end(itineraryId: String) = end(itineraryId, nowUtcZMillis())

    @RequiresApi(Build.VERSION_CODES.O)
    fun endByScheduleItID(scheduleItID: String) {
        val endTimeIso = nowUtcZMillis()

        // Tìm itineraryId tương ứng để update UI local (nếu cần)
        val itineraryId = _ui.value.rt.entries.firstOrNull { it.value.scheduleItID == scheduleItID }?.key

        viewModelScope.launch {
            _ui.update { it.copy(isActing = true, actMessage = null, error = null) }
            when (val res = repo.endItinerary(scheduleItID, EndItineraryRequest(endTimeIso))) {
                is Resource.Success -> {
                    _ui.update {
                        it.copy(
                            isActing = false,
                            actMessage = res.data.message,
                            rt = if (itineraryId != null) {
                                it.rt.toMutableMap().apply {
                                    this[itineraryId] = (this[itineraryId] ?: RuntimeState(scheduleItID))
                                        .copy(endTime = endTimeIso)
                                }
                            } else it.rt
                        )
                    }
                }
                is Resource.Error -> _ui.update { it.copy(isActing = false, error = res.cause) }
                else -> {}
            }
        }
    }

    /** Dùng để bắn message ra snackbar từ UI khi thiếu dữ liệu */
    fun notify(msg: String) {
        _ui.update { it.copy(actMessage = msg) }
    }

    fun end(itineraryId: String, endTimeIso: String) {
        val cur = _ui.value.rt[itineraryId]
        val scheduleItID = cur?.scheduleItID

        if (scheduleItID == null || cur.startTime == null) {
            _ui.update { it.copy(actMessage = "Bạn cần bấm ‘Bắt đầu’ trước khi ‘Rời đi’.") }
            return
        }

        viewModelScope.launch {
            _ui.update { it.copy(isActing = true, actMessage = null, error = null) }
            when (val res = repo.endItinerary(scheduleItID, EndItineraryRequest(endTime = endTimeIso))) {
                is Resource.Success -> {
                    _ui.update {
                        it.copy(
                            isActing = false,
                            actMessage = res.data.message,
                            rt = it.rt.toMutableMap().apply {
                                this[itineraryId] = (this[itineraryId] ?: RuntimeState(scheduleItID = scheduleItID))
                                    .copy(endTime = endTimeIso)
                            }
                        )
                    }
                }
                is Resource.Error -> _ui.update { it.copy(isActing = false, error = res.cause) }
                else -> {}
            }
        }
    }

    fun clearToast() { _ui.update { it.copy(actMessage = null, error = null) } }
}
