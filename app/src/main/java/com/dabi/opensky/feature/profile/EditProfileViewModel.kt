package com.dabi.opensky.feature.profile

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.model.response.User
import com.dabi.opensky.core.data.repository.ProfileRepository
import com.dabi.opensky.core.model.UpdateProfileResponse
import com.dabi.opensky.core.session.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate

sealed interface EditProfileUiState {
    object Idle: EditProfileUiState
    object Loading: EditProfileUiState
    data class Error(val cause: Throwable): EditProfileUiState
    data class Done(val user: UpdateProfileResponse): EditProfileUiState
}

@RequiresApi(Build.VERSION_CODES.O)
@HiltViewModel
class EditProfileViewModel @javax.inject.Inject constructor(
    private val repo: ProfileRepository,
    private val session: SessionStore
) : ViewModel() {

    val user: StateFlow<User?> = session.state.map { it.user }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000), null
    )

    var fullName by mutableStateOf(user.value?.fullName)
        private set
    var phone by mutableStateOf(user.value?.phoneNumber)
        private set
    var citizenId by mutableStateOf(user.value?.citizenId)
        private set
    var dob by mutableStateOf<LocalDate?>((user.value?.doB as LocalDate?))
        private set
    var avatarUri by mutableStateOf<Uri?>(user.value?.avatarURL?.toUri())
        private set

    var uiState by mutableStateOf<EditProfileUiState>(EditProfileUiState.Idle)
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun initFrom(user: User?) {
        if (uiState != EditProfileUiState.Idle) return
        fullName = user?.fullName ?: ""
        phone = user?.phoneNumber ?: ""
        citizenId = user?.citizenId ?: ""
        dob = user?.doB?.let { LocalDate.parse(it) }
        avatarUri = user?.avatarURL?.let { Uri.parse(it) }
    }

    fun onFullName(v: String) { fullName = v }
    fun onPhone(v: String) { phone = v }
    fun onCitizen(v: String) { citizenId = v }
    fun onDob(v: LocalDate?) { dob = v }
    fun onAvatar(uri: Uri?) { avatarUri = uri }

    fun submit(pickResolver: (Uri) -> File?) {
        viewModelScope.launch {
            uiState = EditProfileUiState.Loading
            val file = avatarUri?.let(pickResolver)
            val res = repo.updateProfile(
                fullName = fullName?.trim(),
                phoneNumber = phone?.trim()?.ifBlank { null },
                citizenId = citizenId?.trim()?.ifBlank { null },
                dob = dob?.toString(), // yyyy-MM-dd
                avatarFile = file
            )
            uiState = when(res) {
                is Resource.Success -> EditProfileUiState.Done(res.data)
                is Resource.Error -> EditProfileUiState.Error(res.cause)
                Resource.Loading -> EditProfileUiState.Loading
            }
        }
    }
}