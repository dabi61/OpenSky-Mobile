package com.dabi.opensky.feature.login

import android.util.Patterns
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Travel‑themed Auth Screen combining Login & Sign‑Up in one place.
 * - Gradient + header photo
 * - Tab switch between Đăng nhập / Đăng ký
 * - Subtle animations, form validation, terms checkbox, social buttons
 * - Works with existing LoginViewModel (add register(..) + social stubs)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)

    // Navigate on successful auth
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) onLoginSuccess()
    }

    val keyboard = LocalSoftwareKeyboardController.current
    var selectedTab by rememberSaveable { mutableStateOf(AuthTab.Login) }

    // >>> NEW: Snackbar host
    val snackbarHostState = remember { SnackbarHostState() }

    // >>> NEW: Lắng nghe cờ đăng ký thành công và show snackbar
    LaunchedEffect(uiState.isRegisterSuccessful) {
        if (uiState.isRegisterSuccessful) {
            snackbarHostState.showSnackbar("Đăng ký thành công! Vui lòng đăng nhập.")
            viewModel.markRegisterHandled()
            // (tuỳ chọn) tự động chuyển qua tab Đăng nhập:
             selectedTab = AuthTab.Login
        }
    }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->

        // Top gradient + hero image
        Box(
            modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0EA5E9), // sky blue
                            Color(0xFF0369A1), // deep ocean blue
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            // Header hero (replace with your drawable e.g., R.drawable.bg_travel_header)
//        Image(
//            painter = painterResource(id = R.drawable.bg_travel_header),
//            contentDescription = null,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(220.dp),
//            contentScale = ContentScale.Crop
//        )

            // Semi‑transparent veil for better contrast on top area
//        Box(
//            Modifier
//                .fillMaxWidth()
//                .height(220.dp)
//                .background(Color.Black.copy(alpha = 0.25f))
//        )

            // Foreground content
            Column(
                Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(16.dp))

                // App brand / tagline
                Text(
                    text = "OpenSky",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Khám phá thế giới của bạn",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f))
                )

                Spacer(Modifier.height(12.dp))

                // Card container holding auth forms
                ElevatedCard(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.elevatedCardElevation(8.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Tabs
                        AuthTabs(selectedTab) { selectedTab = it; viewModel.clearError() }
                        Spacer(Modifier.height(4.dp))

                        // Title per tab
//                    Crossfade(targetState = selectedTab, label = "auth_title") { tab ->
//                        Text(
//                            text = if (tab == AuthTab.Login) "Đăng nhập" else "Tạo tài khoản",
//                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
//                            modifier = Modifier.fillMaxWidth(),
//                            textAlign = TextAlign.Start
//                        )
//                    }

                        Spacer(Modifier.height(8.dp))
                        AnimatedVisibility(
                            visible = uiState.errorMessage != null,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            uiState.errorMessage?.let { error ->
                                AssistChip(
                                    onClick = { viewModel.clearError() },
                                    label = { Text(error, color = MaterialTheme.colorScheme.onErrorContainer) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    )
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        when (selectedTab) {
                            AuthTab.Login -> LoginForm(
                                enabled = !uiState.isLoading,
                                onLogin = { email, pass ->
                                    keyboard?.hide(); viewModel.login(email, pass)
                                }
                            )
                            AuthTab.Register -> RegisterForm(
                                enabled = !uiState.isLoading,
                                onRegister = { name, email, pass ->
                                    keyboard?.hide(); viewModel.register(name, email, pass)
                                }
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                    }
                }

                Spacer(Modifier.height(16.dp))

                // Little capsule badge under the card
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF0EA5E9).copy(alpha = 0.15f))
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Ưu đãi mùa du lịch • Giảm đến 30%",
                        style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.onPrimaryContainer)
                    )
                }
            }

            // Loading overlay
            AnimatedVisibility(visible = uiState.isLoading, enter = fadeIn(), exit = fadeOut()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
    }


}

// -------------------- Tabs --------------------

enum class AuthTab { Login, Register }

@Composable
private fun AuthTabs(selected: AuthTab, onSelect: (AuthTab) -> Unit) {
    val tabs = listOf(AuthTab.Login, AuthTab.Register)
    val titles = listOf("Đăng nhập", "Đăng ký")
    val selectedIndex = when (selected) {
        AuthTab.Login -> 0
        AuthTab.Register -> 1
    }

    TabRow(selectedTabIndex = selectedIndex) {
        tabs.forEachIndexed { index, tab ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onSelect(tab) },
                text = { Text(titles[index]) }
            )
        }
    }
}
// -------------------- Forms --------------------

@Composable
private fun LoginForm(
    enabled: Boolean,
    onLogin: (email: String, password: String) -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val emailError = email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
        singleLine = true,
        isError = emailError,
        supportingText = {
            if (emailError) Text("Email không hợp lệ", color = MaterialTheme.colorScheme.error)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Mật khẩu") },
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Outlined.Close else Icons.Outlined.RemoveRedEye,
                    contentDescription = if (passwordVisible) "Ẩn mật khẩu" else "Hiện mật khẩu"
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onLogin(email.trim(), password) }),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )

    Spacer(Modifier.height(8.dp))

    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = { /* TODO: forgot password flow */ }, enabled = enabled) {
            Text("Quên mật khẩu?")
        }
        Button(
            onClick = { onLogin(email.trim(), password) },
            enabled = enabled && email.isNotBlank() && password.isNotBlank() && !emailError,
            modifier = Modifier.height(50.dp)
        ) { Text("Đăng nhập") }
    }
}

@Composable
private fun RegisterForm(
    enabled: Boolean,
    onRegister: (name: String, email: String, password: String) -> Unit,
) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var showPass by rememberSaveable { mutableStateOf(false) }
    var accepted by rememberSaveable { mutableStateOf(false) }

    val emailError = email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()
    val passwordWeak = password.isNotEmpty() && password.length < 8
    val confirmError = confirm.isNotEmpty() && confirm != password

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Họ và tên") },
        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email") },
        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null) },
        singleLine = true,
        isError = emailError,
        supportingText = { if (emailError) Text("Email không hợp lệ", color = MaterialTheme.colorScheme.error) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Mật khẩu (≥ 8 ký tự)") },
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { showPass = !showPass }) {
                Icon(
                    imageVector = if (showPass) Icons.Outlined.Close else Icons.Outlined.RemoveRedEye,
                    contentDescription = if (showPass) "Ẩn mật khẩu" else "Hiện mật khẩu"
                )
            }
        },
        isError = passwordWeak,
        supportingText = { if (passwordWeak) Text("Mật khẩu quá ngắn", color = MaterialTheme.colorScheme.error) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )

    Spacer(Modifier.height(12.dp))

    OutlinedTextField(
        value = confirm,
        onValueChange = { confirm = it },
        label = { Text("Nhập lại mật khẩu") },
        leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = null) },
        visualTransformation = if (showPass) VisualTransformation.None else PasswordVisualTransformation(),
        isError = confirmError,
        supportingText = { if (confirmError) Text("Mật khẩu nhập lại chưa khớp", color = MaterialTheme.colorScheme.error) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = {
            if (!emailError && !passwordWeak && !confirmError && accepted) onRegister(name.trim(), email.trim(), password)
        }),
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled
    )

    Spacer(Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = accepted, onCheckedChange = { accepted = it }, enabled = enabled)
        Spacer(Modifier.width(8.dp))
        Text(
            "Tôi đồng ý với Điều khoản & Chính sách",
            style = MaterialTheme.typography.bodySmall
        )
    }

    Spacer(Modifier.height(8.dp))

    Button(
        onClick = { onRegister(name.trim(), email.trim(), password) },
        enabled = enabled && name.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirm.isNotBlank() && !emailError && !passwordWeak && !confirmError && accepted,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) { Text("Tạo tài khoản") }
}

// -------------------- Preview Helpers (replace resource id) --------------------

@Suppress("unused")
object R { // for preview in isolation; remove if you use your app R
    object drawable { const val bg_travel_header: Int = android.R.drawable.ic_menu_compass }
}
