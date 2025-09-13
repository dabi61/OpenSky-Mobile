# OpenSky Hotel App - Documentation

## 🏨 **Tổng quan dự án**

OpenSky là một ứng dụng đặt phòng khách sạn được xây dựng bằng **Jetpack Compose** với kiến trúc **Clean Architecture**. Ứng dụng cung cấp trải nghiệm tìm kiếm, xem chi tiết và quản lý khách sạn một cách trực quan và hiện đại.

### **Tính năng chính:**
- ✅ **Authentication System** - Đăng nhập/đăng xuất với token management
- ✅ **Hotel Search** - Tìm kiếm khách sạn với filters nâng cao
- ✅ **Hotel Listing** - Hiển thị danh sách khách sạn với pagination
- ✅ **Favorites** - Quản lý khách sạn yêu thích
- ✅ **User Profile** - Quản lý thông tin cá nhân
- ✅ **Settings** - Cài đặt ứng dụng
- ✅ **Bottom Navigation** - Điều hướng giữa 5 màn hình chính

---

## 🏗️ **Kiến trúc ứng dụng**

### **Clean Architecture Layers:**

```
📱 UI Layer (Compose)
    ├── Screens (Home, Search, Favorites, Profile, Settings)
    ├── ViewModels (HotelViewModel, SessionViewModel, LoginViewModel)
    └── UI Components (Cards, Navigation, Dialogs)
    
🔄 Domain Layer
    ├── Use Cases (Future implementation)
    └── Repository Interfaces
    
💾 Data Layer
    ├── Repositories (HotelRepository, AuthRepository)
    ├── Network APIs (HotelApi, AuthApi, LoginService)
    ├── Local Storage (DataStore for tokens)
    └── Models (Hotel, User, API Responses)
    
⚙️ Core Layer
    ├── DI (Hilt Modules)
    ├── Navigation (Type-safe navigation)
    ├── Authentication (Token management)
    └── Event Management (App-wide events)
```

---

## 🌐 **API Integration**

### **Base URL:**
```
https://opensky-be-production.up.railway.app/
```

### **Hotel Search API:**
```http
GET /hotels/search?q={query}&province={province}&address={address}&stars={stars}&minPrice={minPrice}&maxPrice={maxPrice}&sortBy={sortBy}&sortOrder={sortOrder}&page={page}&limit={limit}
```

### **Response Format:**
```json
{
  "hotels": [
    {
      "hotelID": "870ba71a-4b54-4cef-ad9d-32e6b2b49ac4",
      "hotelName": "hnn Hotel",
      "address": "string",
      "province": "string",
      "latitude": 0,
      "longitude": 0,
      "description": "string",
      "star": 3,
      "status": "Active",
      "createdAt": "2025-09-04T04:50:35.852552Z",
      "images": ["https://res.cloudinary.com/dukmz3hdb/image/upload/v1757041764/hotels/hotels_03f53bb4-0aa8-471e-8cdf-7539db6cc792.jpg"],
      "minPrice": 0.01,
      "maxPrice": 0.01,
      "totalRooms": 1,
      "availableRooms": 1
    }
  ],
  "totalCount": 1,
  "page": 1,
  "limit": 10,
  "totalPages": 1,
  "hasNextPage": false,
  "hasPreviousPage": false
}
```

---

## 📱 **Màn hình ứng dụng**

### **1. 🚀 Splash Screen**
- Kiểm tra session đăng nhập
- Navigate tự động đến Login hoặc Home
- Loading animation

### **2. 🔐 Login Screen**
- Username/Password authentication
- Token-based authentication
- Auto-refresh token khi expired
- Error handling và validation

### **3. 🏠 Home Screen**
- Featured hotels carousel
- All hotels list với pagination
- Hotel cards với thông tin cơ bản
- Search integration
- Logout functionality

### **4. 🔍 Search Screen**
- Real-time search với debounce
- Advanced filters:
  - Province/City
  - Star rating (0-5 stars)
  - Price range
- Empty state handling
- Search results với detailed cards

### **5. ❤️ Favorites Screen**
- Saved favorite hotels
- Remove from favorites
- Empty state với call-to-action
- Integration với local storage (future)

### **6. 👤 Profile Screen**
- User information display
- Statistics (bookings, favorites)
- Account management menu:
  - Personal info
  - Booking history
  - Payment methods
  - Notifications
- Support và app info
- Logout functionality

### **7. ⚙️ Settings Screen**
- App preferences:
  - Dark mode toggle
  - Notification settings
  - Location permissions
- Privacy & Security:
  - Password management
  - Privacy settings
- App information:
  - Version info
  - Terms & conditions
  - Privacy policy
  - Support contact

---

## 🧩 **Component Architecture**

### **1. HotelViewModel**
```kotlin
data class HotelUiState(
    val hotels: List<Hotel> = emptyList(),
    val featuredHotels: List<Hotel> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val selectedProvince: String? = null,
    val selectedStars: Int? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val currentPage: Int = 1,
    val hasNextPage: Boolean = false,
    val totalCount: Int = 0
)
```

**Chức năng:**
- ✅ `loadFeaturedHotels()` - Load khách sạn nổi bật
- ✅ `loadAllHotels()` - Load tất cả khách sạn với pagination
- ✅ `searchHotels(query)` - Tìm kiếm đơn giản
- ✅ `advancedSearch()` - Tìm kiếm với filters
- ✅ `loadMoreHotels()` - Load more cho pagination
- ✅ `clearSearch()` - Reset search về all hotels

### **2. HotelRepository**
```kotlin
suspend fun searchHotels(request: HotelSearchRequest): Result<HotelSearchResponse>
suspend fun getHotelById(hotelId: String): Result<Hotel>
suspend fun getAllHotels(): Result<HotelSearchResponse>
suspend fun getFeaturedHotels(): Result<List<Hotel>>
suspend fun quickSearch(query: String): Result<HotelSearchResponse>
suspend fun advancedSearch(): Result<HotelSearchResponse>
```

### **3. Bottom Navigation**
- 5 tabs: Home, Search, Favorites, Profile, Settings
- Material 3 NavigationBar
- Selected/Unselected icons
- Type-safe navigation với Kotlin Serialization

---

## 🔒 **Authentication System**

### **Token Management:**
- **Access Token** - Short-lived cho API calls
- **Refresh Token** - Long-lived cho token renewal
- **Auto-refresh** - Tự động refresh khi token expired
- **Secure Storage** - DataStore Preferences

### **Session Management:**
```kotlin
data class SessionUiState(
    val showTokenExpiredDialog: Boolean = false,
    val isSessionValid: Boolean? = null,
    val shouldNavigateToLogin: Boolean = false
)
```

### **Event-driven Architecture:**
- `AppEventManager` - Global event bus
- `AppEvent.TokenExpired` - Token expiry notification
- `TokenExpiredDialog` - User notification cho token expiry

---

## 🛠️ **Tech Stack**

### **UI Framework:**
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system
- **Navigation Compose** - Type-safe navigation
- **Coil** - Image loading

### **Architecture:**
- **Clean Architecture** - Separation of concerns
- **MVVM Pattern** - UI state management
- **Hilt** - Dependency injection
- **StateFlow/Flow** - Reactive programming

### **Network:**
- **Retrofit** - HTTP client
- **Moshi** - JSON serialization
- **OkHttp** - HTTP interceptors
- **Custom Authenticator** - Token refresh

### **Storage:**
- **DataStore Preferences** - Token storage
- **Room Database** - Local data (future)

---

## 🚀 **Getting Started**

### **Prerequisites:**
- Android Studio Hedgehog+
- JDK 17+
- Android SDK 34+
- Kotlin 1.9+

### **Setup:**
1. Clone repository
2. Open trong Android Studio
3. Sync Gradle dependencies
4. Run `./gradlew assembleDebug`
5. Install APK trên device/emulator

### **Build Configuration:**
```kotlin
compileSdk = 34
minSdk = 24
targetSdk = 34
```

### **Key Dependencies:**
```kotlin
// Compose BOM
implementation(platform("androidx.compose:compose-bom:2024.10.00"))

// Hilt DI
implementation("com.google.dagger:hilt-android:2.57")

// Network
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.moshi:moshi-kotlin:1.15.2")

// Image Loading
implementation("io.coil-kt:coil-compose:2.7.0")
```

---

## 🎨 **UI/UX Design**

### **Design Principles:**
- **Material 3** design language
- **Consistent spacing** - 4dp grid system
- **Typography scale** - Material 3 type system
- **Color scheme** - Dynamic theming support
- **Accessibility** - Content descriptions, semantic roles

### **Component Library:**
- **HotelCard** - Reusable hotel display component
- **FeaturedHotelCard** - Carousel hotel card
- **SearchHotelCard** - Search result card
- **BottomNavigation** - 5-tab navigation
- **FilterSection** - Advanced search filters
- **TokenExpiredDialog** - Session management

### **Loading States:**
- **Shimmer loading** - Skeleton screens
- **Pagination loading** - Load more indicators
- **Error states** - User-friendly error messages
- **Empty states** - Meaningful empty screens

---

## 🧪 **Testing Strategy**

### **Unit Tests:**
- ViewModel logic testing
- Repository testing với MockWebServer
- Use case testing (future)

### **Integration Tests:**
- API integration testing
- Database operations
- Navigation flows

### **UI Tests:**
- Screen rendering tests
- User interaction flows
- Accessibility testing

---

## 🚀 **Deployment**

### **Build Types:**
- **Debug** - Development build với logging
- **Release** - Production build với ProGuard

### **CI/CD Pipeline:**
- GitHub Actions cho automated testing
- Automated APK generation
- Code quality checks với detekt

---

## 📊 **Performance Optimization**

### **Image Loading:**
- **Coil** với memory caching
- **Placeholder images** cho loading states
- **Error fallbacks** cho failed loads

### **List Performance:**
- **LazyColumn** cho efficient scrolling
- **Pagination** để giảm memory usage
- **State hoisting** cho recomposition optimization

### **Network Optimization:**
- **Request caching** với OkHttp
- **Retry logic** cho failed requests
- **Connection pooling** cho efficiency

---

## 🔮 **Future Enhancements**

### **Phase 2 Features:**
- [ ] **Hotel Detail Screen** - Chi tiết khách sạn đầy đủ
- [ ] **Booking System** - Đặt phòng integration
- [ ] **Payment Integration** - Thanh toán trực tuyến
- [ ] **Push Notifications** - Real-time updates
- [ ] **Offline Support** - Local caching với Room
- [ ] **Map Integration** - Google Maps cho location
- [ ] **Reviews & Ratings** - User feedback system
- [ ] **Social Features** - Share hotels, reviews

### **Technical Improvements:**
- [ ] **Use Cases** - Domain layer implementation
- [ ] **Unit Tests** - Comprehensive test coverage
- [ ] **Performance Monitoring** - Firebase Performance
- [ ] **Analytics** - User behavior tracking
- [ ] **Crashlytics** - Error reporting
- [ ] **Feature Flags** - A/B testing support

---

## 📞 **Support & Contact**

### **Development Team:**
- **Lead Developer:** [Your Name]
- **Email:** developer@opensky.com
- **GitHub:** https://github.com/your-repo/opensky-android

### **API Documentation:**
- **Backend API:** [API Docs URL]
- **Postman Collection:** [Collection Link]

### **Bug Reports:**
- **GitHub Issues:** [Issues URL]
- **Email:** bugs@opensky.com

---

## 📄 **License**

```
MIT License

Copyright (c) 2024 OpenSky Hotel App

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

---

## 🎉 **Kết luận**

OpenSky Hotel App được xây dựng với **modern Android development practices**, sử dụng **Jetpack Compose** và **Clean Architecture**. Ứng dụng cung cấp trải nghiệm người dùng mượt mà với **token-based authentication**, **real-time search**, và **intuitive navigation**.

**Điểm mạnh của dự án:**
- ✅ **Scalable Architecture** - Dễ mở rộng và maintain
- ✅ **Modern UI** - Material 3 design system
- ✅ **Robust Authentication** - Secure token management
- ✅ **Performance Optimized** - Efficient data loading
- ✅ **Production Ready** - Error handling và edge cases

Ứng dụng sẵn sàng cho việc deployment và có thể dễ dàng mở rộng thêm các tính năng mới trong tương lai! 🚀
