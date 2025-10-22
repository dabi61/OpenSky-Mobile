# Kiến trúc Authentication - Giải thích chi tiết

## 1. Tại sao tách SessionStore và AuthRepository?

### SessionStore (Low-level Storage)
```kotlin
interface SessionStore {
    val state: StateFlow<AuthResponse>
    fun getAccess(): String?
    fun getRefresh(): String?
    suspend fun update(newTokens: AuthResponse?)
    suspend fun clear()
}
```

**Trách nhiệm:**
-  Lưu trữ token vào DataStore (persistence)
-  Cung cấp StateFlow để observe token state
-  Thread-safe operations
-  Cache token trong memory

### AuthRepository (Business Logic)
```kotlin
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<Unit>
    suspend fun logout()
    val isLoggedIn: Flow<Boolean>
}
```

**Trách nhiệm:**
-  Gọi API login/logout
-  Parse response và convert sang internal format
-  Business logic validation
-  Handle errors và retry logic

### Lợi ích của việc tách:

#### 🔄 **Single Responsibility Principle**
- SessionStore: Chỉ lo storage
- AuthRepository: Chỉ lo business logic

#### 🧪 **Testability**
```kotlin
// Test SessionStore riêng biệt
@Test fun `should save token to datastore`() { }

// Test AuthRepository với mock SessionStore
@Test fun `should login successfully`() { }
```

#### 🔄 **Reusability**
```kotlin
class ProfileRepository(
    private val sessionStore: SessionStore // Reuse storage
) {
    fun getCurrentUser() = sessionStore.state.map { it.user }
}
```

#### 🏗️ **Dependency Inversion**
```kotlin
class TokenAuthenticator(
    private val sessionStore: SessionStore // Depend on abstraction
) {
    // Không phụ thuộc vào AuthRepository
}
```

## 2. Cách sử dụng API khác với Authentication

### Tự động thêm token (Recommended)

Tất cả API calls đã được setup tự động:

```kotlin
interface ProductApi {
    @GET("products")
    suspend fun getProducts(): Response<List<Product>>
    
    @POST("products")
    suspend fun createProduct(@Body product: Product): Response<Product>
    
    @PUT("products/{id}")
    suspend fun updateProduct(@Path("id") id: String, @Body product: Product): Response<Product>
}
```

**AuthInterceptor** tự động thêm token:
```
Authorization: Bearer <access_token>
```

**TokenAuthenticator** tự động refresh khi 401:
1. Detect 401 → Call refresh API
2. Update new token → Retry original request

### Ví dụ sử dụng:

```kotlin
@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productApi: ProductApi // Token tự động được thêm
) : ViewModel() {
    
    fun loadProducts() {
        viewModelScope.launch {
            try {
                val response = productApi.getProducts() // Token tự động
                if (response.isSuccessful) {
                    // Handle success
                }
            } catch (e: Exception) {
                // Handle error (token expired sẽ được xử lý tự động)
            }
        }
    }
}
```

### Manual token (Nếu cần)

```kotlin
class CustomRepository @Inject constructor(
    private val sessionStore: SessionStore,
    private val apiService: ApiService
) {
    suspend fun callSpecialApi() {
        val token = sessionStore.getAccess()
        val response = apiService.specialCall("Bearer $token")
    }
}
```

## 3. Navigation Architecture

### Hai layer navigation:

#### Layer 1: OpenSkyNavigation.kt (Compose Navigation)
```kotlin
fun NavGraphBuilder.openSkyNavigation(navController: NavHostController) {
    composable<OpenSkyScreen.Login> { LoginScreen() }
    composable<OpenSkyScreen.Home> { HomeScreen() }
}
```
-  Standard Compose Navigation
-  Type-safe với Kotlin Serialization
-  Simple và direct

#### Layer 2: core/navigation/ (Custom Navigation)
```kotlin
class OpenSkyComposeNavigator : AppComposeNavigator<OpenSkyScreen>() {
    override fun navigate(route: OpenSkyScreen) { }
    override fun navigateBackWithResult() { }
}
```

### Tại sao có 2 layer?

#### 🎯 **Flexibility**
```kotlin
// Simple navigation
navController.navigate(OpenSkyScreen.Home)

// Complex navigation với result
navigator.navigateBackWithResult("key", result, OpenSkyScreen.Home)
```

#### 🧪 **Testability**
```kotlin
class MockNavigator : AppComposeNavigator<OpenSkyScreen>() {
    val navigatedRoutes = mutableListOf<OpenSkyScreen>()
    override fun navigate(route: OpenSkyScreen) {
        navigatedRoutes.add(route)
    }
}
```

#### 🔄 **Abstraction**
```kotlin
// ViewModel không phụ thuộc vào Compose Navigation
class LoginViewModel(
    private val navigator: AppComposeNavigator<OpenSkyScreen>
) {
    fun onLoginSuccess() {
        navigator.navigate(OpenSkyScreen.Home) // Abstract
    }
}
```

### Khi nào dùng gì?

####  **Dùng Compose Navigation khi:**
- Simple navigation giữa screens
- Không cần result callback
- Không cần testing navigation logic

####  **Dùng Custom Navigator khi:**
- Cần navigate với result
- Cần testing navigation
- Complex navigation flows
- Shared navigation logic

## Kết luận

### Current Setup (Recommended):
```
UI Layer (Compose)
    ↓
Repository Layer (Business Logic)
    ↓
SessionStore Layer (Storage)
    ↓
DataStore (Persistence)
```

### Benefits:
-  **Separation of Concerns**
-  **Testable Architecture**
-  **Reusable Components**
-  **Type Safety**
-  **Automatic Token Management**
-  **Flexible Navigation**

Bạn có thể sử dụng cả 2 layer navigation tùy theo nhu cầu của từng screen!
