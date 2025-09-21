package com.dabi.opensky.core.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AppEvent {
    object TokenExpired : AppEvent()
}

@Singleton
class AppEventManager @Inject constructor() {
    private val _events = MutableSharedFlow<AppEvent>()
    val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    fun emitEvent(event: AppEvent) {
        _events.tryEmit(event)
    }
}
