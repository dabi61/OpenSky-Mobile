# Hệ thống Authentication cho OpenSky App

## Tổng quan

Tôi đã thiết kế và triển khai một hệ thống authentication hoàn chỉnh cho dự án OpenSky của bạn với các tính năng:

-  Xử lý token tự động (access token + refresh token)
-  Auto refresh token khi access token hết hạn
-  Lưu trữ an toàn token bằng DataStore
-  UI đăng nhập đẹp mắt với validation
-  Navigation tự động dựa trên trạng thái đăng nhập
-  Interceptor tự động thêm token vào request
-  Error handling toàn diện

## Cấu trúc hệ thống

### 1. Data Models

**AuthResponse.kt** - Model chính để lưu trữ token:
```kotlin
data class AuthResponse(
    val access: String?,
    val refresh: String?,
    val accessExpSec: Long?,
    val refreshExpSec: Long?,
)
```

**LoginResponse.kt** - Model cho response từ API login:
```kotlin
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpires: String,
    val refreshTokenExpires: String,
    val user: User
)
```

**User.kt** - Model cho thông tin user:
```kotlin
data class User(
    val userID: String,
    val email: String,
    val fullName: String,
    val role: String,
    val status: Int,
    // ... các field khác
)
```

### 2. Session Management

**SessionStore** - Interface quản lý session:
- `getAccess()` - Lấy access token hiện tại
- `getRefresh()` - Lấy refresh token
- `update()` - Cập nhật token mới
- `clear()` - Xóa session (logout)
- `isAccessExpiringSoon()` - Kiểm tra token sắp hết hạn

**TokenDataSource** - Lưu trữ token vào DataStore an toàn

### 3. Network Layer

**AuthInterceptor** - Tự động thêm Bearer token vào header:
- Skip các endpoint không cần auth (`/auth/login`, `/auth/refresh`)
- Tự động thêm `Authorization: Bearer <token>` vào request

**TokenAuthenticator** - Tự động refresh token khi 401:
- Detect khi access token hết hạn (401 Unauthorized)
- Tự động gọi API refresh token
- Retry request với token mới
- Thread-safe với ReentrantLock
- Backoff strategy để tránh spam API

### 4. Repository Pattern

**AuthRepository** - Interface cho authentication logic:
```kotlin
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<Unit>
    suspend fun logout()
    val isLoggedIn: Flow<Boolean>
}
```

**AuthRepositoryImpl** - Implementation:
- Gọi API login
- Parse response và lưu token
- Emit trạng thái đăng nhập qua Flow

### 5. UI Layer

**LoginViewModel** - Quản lý state và logic cho màn hình login:
- Loading state
- Error handling
- Form validation
- Auto navigation sau khi login thành công

**LoginScreen** - UI đẹp mắt với:
- Username/Password fields
- Show/hide password
- Loading indicator
- Error messages
- Keyboard handling
- Auto focus management

**HomeScreen** - Màn hình chính với nút logout để test

### 6. Navigation

**OpenSkyNavHost** - Navigation chính:
- Tự động navigate đến Login nếu chưa đăng nhập
- Navigate đến Home nếu đã đăng nhập
- Observe `isLoggedIn` state để tự động chuyển màn hình

## Cách sử dụng

### 1. Đăng nhập
```kotlin
// Trong LoginScreen, user nhập username/password
// ViewModel sẽ gọi:
authRepository.login(username, password)

// Nếu thành công, token được lưu tự động
// Navigation tự động chuyển đến HomeScreen
```

### 2. API Calls
```kotlin
// Tất cả API calls sẽ tự động có token trong header
@GET("api/protected-endpoint")
suspend fun getProtectedData(): Response<Data>

// AuthInterceptor tự động thêm: Authorization: Bearer <token>
// Nếu token hết hạn, TokenAuthenticator tự động refresh và retry
```

### 3. Đăng xuất
```kotlin
// Trong HomeScreen hoặc bất kỳ đâu:
loginViewModel.logout()

// Sẽ xóa token và tự động navigate về LoginScreen
```

## Luồng hoạt động

### Login Flow:
1. User nhập username/password trong LoginScreen
2. LoginViewModel gọi `authRepository.login()`
3. AuthRepository gọi API `/auth/login`
4. Parse response và lưu token vào SessionStore
5. SessionStore emit `isLoggedIn = true`
6. NavHost detect và navigate đến HomeScreen

### API Request Flow:
1. App gọi API endpoint bất kỳ
2. AuthInterceptor thêm Bearer token vào header
3. Nếu server trả 401 (token hết hạn):
    - TokenAuthenticator detect 401
    - Tự động gọi `/auth/refresh` với refresh token
    - Parse response và update access token mới
    - Retry original request với token mới
4. Nếu refresh token cũng hết hạn:
    - Clear session
    - User tự động navigate về LoginScreen

### Logout Flow:
1. User click logout button
2. ViewModel gọi `authRepository.logout()`
3. SessionStore clear all tokens
4. Emit `isLoggedIn = false`
5. NavHost detect và navigate về LoginScreen

## Bảo mật

-  Token được lưu trong EncryptedDataStore
-  Không log token trong debug
-  Thread-safe operations
-  Auto clear token khi hết hạn
-  Retry mechanism với backoff
-  Proper error handling

## Testing

Để test hệ thống:

1. **Build và chạy app**
2. **Màn hình Login sẽ xuất hiện đầu tiên**
3. **Nhập username/password** (sử dụng credentials hợp lệ từ API của bạn)
4. **App sẽ tự động navigate đến HomeScreen** nếu login thành công
5. **Click "Đăng xuất"** để test logout flow
6. **App tự động quay về LoginScreen**

## Mở rộng

Để thêm các tính năng khác:

1. **Thêm API endpoints mới**: Chỉ cần tạo interface Retrofit, token sẽ tự động được thêm
2. **Thêm màn hình mới**: Thêm vào `OpenSkyScreen` và `openSkyNavigation()`
3. **Thêm user profile**: Mở rộng `User` data class và tạo repository/viewmodel tương ứng
4. **Remember me**: Thêm checkbox trong LoginScreen và logic lưu credentials

Hệ thống đã được thiết kế hoàn chỉnh và sẵn sàng để sử dụng! 🚀
