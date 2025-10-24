package com.dabi.opensky.feature.login

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.repository.login.AuthRepository
import com.dabi.opensky.core.data.remote.model.response.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccessful: Boolean = false,

    // >>> Thêm state cho Đăng ký
    val isRegisterSuccessful: Boolean = false,
    val registeredUser: User? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    val isLoggedIn = authRepository.isLoggedIn

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Vui lòng nhập đầy đủ thông tin"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = authRepository.login(username, password)
                if (result.isSuccess) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccessful = true,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Đăng nhập thất bại" ?: result.exceptionOrNull()?.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Có lỗi xảy ra"
                )
            }
        }
    }

    // >>> NEW: Đăng ký
    fun register(fullName: String, email: String, password: String) {
        // Validate cơ bản phía client
        when {
            fullName.isBlank() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Vui lòng nhập họ tên")
                return
            }
            email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Email không hợp lệ")
                return
            }
            password.length < 8 -> {
                _uiState.value = _uiState.value.copy(errorMessage = "Mật khẩu cần ≥ 8 ký tự")
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = authRepository.register(fullName, email, password)
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isRegisterSuccessful = true,
                        registeredUser = user,
                        errorMessage = null
                    )
                    // Lưu ý: API register không trả token => không set session.
                    // UI nên chuyển tab sang Đăng nhập hoặc tự gọi login() nếu muốn auto-login.
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Đăng ký thất bại"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Có lỗi xảy ra"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
    /** Gọi sau khi UI đã hiển thị snackbar để reset cờ tránh lặp lại */
    fun markRegisterHandled() {
        _uiState.value = _uiState.value.copy(isRegisterSuccessful = false)
    }
}
