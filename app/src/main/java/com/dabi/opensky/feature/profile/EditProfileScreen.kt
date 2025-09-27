package com.dabi.opensky.feature.profile

import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dabi.opensky.core.data.remote.model.response.User
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Edit screen gắn chặt với ProfileViewModel bạn đã cung cấp.
 * - Dùng trực tiếp state trong VM (isEditing, fullName, phone, citizenId, dob, avatarUri, editState)
 * - Gọi vm.startEdit(user) để fill form
 * - Gọi vm.save { uri -> File } để submit multipart
 */

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: ProfileViewModel = hiltViewModel()
) {
    val user by vm.user.collectAsState()
    val context = LocalContext.current

    // init form từ user khi mở màn (chỉ 1 lần / theo userID)
    LaunchedEffect(user?.userID) { vm.startEdit(user) }

    var showDatePicker by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) vm.onAvatar(uri)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sửa hồ sơ") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) } },
                actions = {
                    IconButton(
                        enabled = vm.editState !is EditState.Saving,
                        onClick = {
                            vm.save { uri: Uri ->
                                // Convert content Uri -> File (copy vào cache)
                                val input = context.contentResolver.openInputStream(uri) ?: return@save null
                                val tmp = File.createTempFile("avatar_", ".jpg", context.cacheDir)
                                tmp.outputStream().use { out -> input.copyTo(out) }
                                tmp
                            }
                            onBack()
                        }
                    ) { Icon(Icons.Default.Check, null) }
                }
            )
        }
    ) { pv ->
        Column(
            modifier = modifier
                .padding(pv)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Avatar
            Row(verticalAlignment = Alignment.CenterVertically) {
                val req = ImageRequest.Builder(LocalContext.current)
                    .data(vm.avatarUri ?: user?.avatarURL)
                    .crossfade(true)
                    .build()
                AsyncImage(
                    model = req,
                    contentDescription = null,
                    modifier = Modifier.size(96.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                TextButton(onClick = { imagePicker.launch("image/*") }, enabled = vm.editState !is EditState.Saving) {
                    Icon(Icons.Default.Create, null)
                    Spacer(Modifier.width(6.dp))
                    Text("Đổi ảnh")
                }
            }

            OutlinedTextField(
                value = vm.fullName,
                onValueChange = vm::onFullName,
                label = { Text("Họ và tên") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = vm.editState !is EditState.Saving
            )

            OutlinedTextField(
                value = vm.phone,
                onValueChange = vm::onPhone,
                label = { Text("Số điện thoại") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = vm.editState !is EditState.Saving
            )

            OutlinedTextField(
                value = vm.citizenId,
                onValueChange = vm::onCitizen,
                label = { Text("CCCD/CMND") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = vm.editState !is EditState.Saving
            )

            OutlinedTextField(
                value = vm.dob?.toString() ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Ngày sinh (yyyy-MM-dd)") },
                trailingIcon = {
                    TextButton(onClick = { showDatePicker = true }, enabled = vm.editState !is EditState.Saving) {
                        Text("Chọn")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = true
            )

            if (vm.editState is EditState.Saving) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            val error = (vm.editState as? EditState.Error)?.cause?.message
            if (!error.isNullOrBlank()) {
                Text("Lỗi: $error", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Hủy") }
                }
            ) {
                val state = rememberDatePickerState()
                DatePicker(state = state)
                LaunchedEffect(state.selectedDateMillis) {
                    val millis = state.selectedDateMillis
                    if (millis != null) {
                        val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        vm.onDob(localDate)
                    }
                }
            }
        }
    }
}
