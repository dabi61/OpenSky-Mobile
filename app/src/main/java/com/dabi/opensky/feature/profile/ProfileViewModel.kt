package com.dabi.opensky.feature.profile

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.model.response.User
import com.dabi.opensky.core.data.repository.ProfileRepository
import com.dabi.opensky.core.session.SessionStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

/* ================= ViewModel (single for view+edit) ================= */
sealed interface EditState { object None: EditState; object Saving: EditState; data class Error(val cause: Throwable): EditState }

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val session: SessionStore,
    private val repo: ProfileRepository
) : ViewModel() {
    val user: StateFlow<User?> = session.state.map { it.user }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    // Form states
    var isEditing by mutableStateOf(false); private set
    var fullName by mutableStateOf(""); private set
    var phone by mutableStateOf(""); private set
    var citizenId by mutableStateOf(""); private set
    var dob by mutableStateOf<LocalDate?>(null); private set
    var avatarUri by mutableStateOf<Uri?>(null); private set

    var editState by mutableStateOf<EditState>(EditState.None); private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun startEdit(u: User?) {
        if (isEditing) return
        isEditing = true
        fullName = u?.fullName ?: ""
        phone = u?.phoneNumber ?: ""
        citizenId = u?.citizenId ?: ""
        dob = u?.doB?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        avatarUri = u?.avatarURL?.let { runCatching { Uri.parse(it) }.getOrNull() }
    }
    fun cancelEdit() { isEditing = false; editState = EditState.None }

    fun onFullName(v:String){ fullName=v }
    fun onPhone(v:String){ phone=v }
    fun onCitizen(v:String){ citizenId=v }
    fun onDob(v:LocalDate?){ dob=v }
    fun onAvatar(uri:Uri?){ avatarUri=uri }

    fun save(resolve: (Uri)-> File?) {
        val current = user.value ?: return
        viewModelScope.launch {
            editState = EditState.Saving
            val file = withContext(Dispatchers.IO) { avatarUri?.let(resolve) }
            when(val res = repo.updateProfile(fullName.trim(), phone.trim().ifBlank{null}, citizenId.trim().ifBlank{null}, dob?.toString(), file)){
                is Resource.Success -> {
                    // Map UpdateProfileResponse -> User (cùng schema)
                    val updated = User(
                        userID = res.data.profile.userID,
                        email = res.data.profile.email,
                        fullName = res.data.profile.fullName,
                        role = res.data.profile.role,
                        phoneNumber = res.data.profile.phoneNumber,
                        citizenId = res.data.profile.citizenId,
                        doB = res.data.profile.dob,
                        avatarURL = res.data.profile.avatarURL,
                        status = current.status,
                        createdAt = res.data.profile.createdAt
                    )
                    // Update SessionStore giữ nguyên token
                    session.setUser(updated)
//                    Log.d("ProfileViewModel", "save: $updated")
                    editState = EditState.None
                    isEditing = false
                }
                is Resource.Error -> {
                    editState = EditState.Error(res.cause)
                    Log.d("ProfileViewModel", "save: ${res.cause}")
                }
                Resource.Loading -> { /* not used */ }
            }
        }
    }
}