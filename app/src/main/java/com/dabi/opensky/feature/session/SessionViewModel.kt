package com.dabi.opensky.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.repository.login.AuthRepository
import com.dabi.opensky.core.event.AppEvent
import com.dabi.opensky.core.event.AppEventManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionUiState(
    val showTokenExpiredDialog: Boolean = false,
    val isSessionValid: Boolean? = null // null = loading, true = valid, false = invalid
)

@HiltViewModel
class SessionViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val eventManager: AppEventManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    val isLoggedIn = authRepository.isLoggedIn

    init {
        // Listen for token expiry events
        viewModelScope.launch {
            eventManager.events.collect { event ->
                when (event) {
                    is AppEvent.TokenExpired -> {
                        showTokenExpiredDialog()
                    }
                }
            }
        }
    }

    fun showTokenExpiredDialog() {
        _uiState.value = _uiState.value.copy(showTokenExpiredDialog = true)
    }

    fun dismissTokenExpiredDialog() {
        _uiState.value = _uiState.value.copy(showTokenExpiredDialog = false)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            dismissTokenExpiredDialog()
        }
    }

    companion object {
        const val TOKEN_EXPIRED_EVENT = "token_expired"
    }
}
