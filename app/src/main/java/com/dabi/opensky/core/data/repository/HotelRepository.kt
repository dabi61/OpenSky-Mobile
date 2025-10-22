package com.dabi.opensky.core.data.repository

import com.dabi.opensky.core.data.remote.HttpFailureException
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.HotelService
import com.dabi.opensky.core.data.remote.api.RoomService
import com.dabi.opensky.core.data.remote.apiCall
import com.dabi.opensky.core.data.remote.backoffMillis
import com.dabi.opensky.core.model.hotel.Hotel
import com.dabi.opensky.core.model.hotel.HotelDetailResponse
import com.dabi.opensky.core.model.hotel.HotelSearchRequest
import com.dabi.opensky.core.model.hotel.HotelSearchResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
@Singleton
class HotelRepository @Inject constructor(
    private val hotelApi: HotelService,
) {
    // ---- Featured cache (in-memory) ----
    private val featuredCache = MutableStateFlow<List<Hotel>>(emptyList())

    // ---- Search LRU cache với TTL (áp dụng cho keyword search) ----
    private data class CacheEntry(
        val data: HotelSearchResponse,
        val timestamp: Long
    )

    private val ttlMillis = 5 * 60 * 1000L // 5 phút
    private val maxEntries = 100

    private val lru = object : LinkedHashMap<String, CacheEntry>(maxEntries, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean = size > maxEntries
    }

    private fun isExpired(ts: Long) = (System.currentTimeMillis() - ts) > ttlMillis
    private fun cacheGet(key: String): CacheEntry? = synchronized(lru) { lru[key] }
    private fun cachePut(key: String, entry: CacheEntry) = synchronized(lru) { lru[key] = entry }
    private fun clearExpired() = synchronized(lru) {
        val it = lru.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (isExpired(e.value.timestamp)) it.remove()
        }
    }

    /** Chỉ cache theo keyword/page/limit (đúng contract mới) */
    private fun keyOfKeyword(q: String?, page: Int, limit: Int) = listOf(q.orEmpty(), page, limit).joinToString("|")

    // -------------------------------------------------------------
    // Public APIs
    // -------------------------------------------------------------

    fun getFeaturedHotels(limit: Int = 5): Flow<Resource<List<Hotel>>> = flow {
        // emit cache trước
        val cached = featuredCache.value
        if (cached.isNotEmpty()) emit(Resource.Success(cached))
        emit(Resource.Loading)

        // (tùy backend) Nếu bạn vẫn có endpoint featured riêng thì giữ;
        // còn không có thì fallback sang search null
        when (val res = apiCall { hotelApi.getFeaturedHotels(limit = limit, page = 1) }) {
            is Resource.Success -> {
                val hotels = res.data.hotels
                if (hotels.isNotEmpty()) featuredCache.value = hotels
                emit(Resource.Success(hotels))
            }
            is Resource.Error -> emit(res)
            Resource.Loading -> Unit
        }
    }.retryWhen { cause, attempt ->
        val retryable = cause is IOException
        val http = (cause as? HttpFailureException)?.code
        val shouldRetry = retryable && attempt < 3 && http !in listOf(400, 401, 403, 404)
        if (shouldRetry) delay(backoffMillis(attempt))
        shouldRetry
    }

    fun getHotelById(id: String): Flow<Resource<HotelDetailResponse>> = flow {
        println("HotelRepository: Getting hotel by ID: $id")
        emit(Resource.Loading)

        var result = apiCall {
            println("HotelRepository: Making API call to /hotels/$id")
            hotelApi.getHotelById(id)
        }

        if (result is Resource.Error &&
            result.cause is HttpFailureException &&
            result.cause.code == 404
        ) {
            println("HotelRepository: Primary endpoint failed, trying alternative /hotel/$id")
            result = apiCall { hotelApi.getHotelById(id) }
        }

        when (result) {
            is Resource.Success -> {
                println("HotelRepository: Successfully got hotel: ${result.data.hotelName}")
                emit(result)
            }
            is Resource.Error -> {
                println("HotelRepository: API call failed: ${result.cause.message}")
                if (result.cause is HttpFailureException) {
                    println("HotelRepository: HTTP Error Code: ${result.cause.code}")
                }
                emit(result)
            }
            Resource.Loading -> emit(result)
        }
    }.retryWhen { cause, attempt ->
        val retryable = cause is IOException
        val http = (cause as? HttpFailureException)?.code
        val shouldRetry = retryable && attempt < 2 && http !in listOf(400, 404)
        if (shouldRetry) {
            println("HotelRepository: Retrying API call (attempt ${attempt + 1})")
            delay(backoffMillis(attempt))
        }
        shouldRetry
    }

    /**
     * Lấy "tất cả" (theo paging) -> backend hiện dùng /hotels/search với q=null
     */
    fun getAllHotels(
        page: Int = 1,
        limit: Int = 10
    ): Flow<Resource<HotelSearchResponse>> = flow {
        emit(Resource.Loading)
        emit(apiCall { hotelApi.searchHotels(keyword = null, page = page, limit = limit) })
    }.retryWhen { cause, attempt ->
        val retryable = cause is IOException
        val http = (cause as? HttpFailureException)?.code
        val shouldRetry = retryable && attempt < 3 && http !in listOf(400, 404)
        if (shouldRetry) delay(backoffMillis(attempt))
        shouldRetry
    }

    /**
     * Search theo keyword (đúng contract mới: chỉ q, page, limit)
     * Vẫn giữ cache LRU cho keyword search.
     */
    fun searchHotels(request: HotelSearchRequest): Flow<Resource<HotelSearchResponse>> = flow {
        emit(Resource.Loading)
        clearExpired()

        val q = request.q
        val page = request.page
        val limit = request.limit
        val key = keyOfKeyword(q, page, limit)

        cacheGet(key)?.takeIf { !isExpired(it.timestamp) }?.let {
            emit(Resource.Success(it.data))
            return@flow
        }

        when (val res = apiCall { hotelApi.searchHotels(keyword = q, page = page, limit = limit) }) {
            is Resource.Success -> {
                cachePut(key, CacheEntry(res.data, System.currentTimeMillis()))
                emit(res)
            }
            is Resource.Error -> emit(res)
            Resource.Loading -> Unit
        }
    }.retryWhen { cause, attempt ->
        val retryable = cause is IOException
        val http = (cause as? HttpFailureException)?.code
        val shouldRetry = retryable && attempt < 3 && http !in listOf(400, 404)
        if (shouldRetry) delay(backoffMillis(attempt))
        shouldRetry
    }

    /**
     * Danh sách tỉnh
     */
    fun getProvinces(): Flow<Resource<List<String>>> = flow {
        emit(Resource.Loading)
        when (val res = apiCall { hotelApi.getProvinces() }) {
            is Resource.Success -> emit(Resource.Success(res.data.provinces))
            is Resource.Error -> emit(res)
            Resource.Loading -> Unit
        }
    }.retryWhen { cause, attempt ->
        val retryable = cause is IOException
        val http = (cause as? HttpFailureException)?.code
        val shouldRetry = retryable && attempt < 2 && http !in listOf(400, 404)
        if (shouldRetry) delay(backoffMillis(attempt))
        shouldRetry
    }

    /**
     * Lấy hotel theo TỈNH (shape: currentPage/pageSize/…)
     * Map về HotelSearchResponse để UI tái dùng.
     */
    fun getHotelsByProvince(
        province: String,
        page: Int = 1,
        size: Int = 10
    ): Flow<Resource<HotelSearchResponse>> = flow {
        emit(Resource.Loading)
        when (val res = apiCall { hotelApi.getHotelsByProvince(province, page, size) }) {
            is Resource.Success -> emit(Resource.Success(res.data.toHotelSearchResponse()))
            is Resource.Error -> emit(res)
            Resource.Loading -> Unit
        }
    }.retryWhen { cause, attempt ->
        val retryable = cause is IOException
        val http = (cause as? HttpFailureException)?.code
        val shouldRetry = retryable && attempt < 2 && http !in listOf(400, 404)
        if (shouldRetry) delay(backoffMillis(attempt))
        shouldRetry
    }

    /**
     * Lấy hotel theo SỐ SAO (shape tương tự /province)
     */
    fun getHotelsByStars(
        stars: Int,
        page: Int = 1,
        size: Int = 10
    ): Flow<Resource<HotelSearchResponse>> = flow {
        emit(Resource.Loading)
        when (val res = apiCall { hotelApi.getHotelsByStars(stars, page, size) }) {
            is Resource.Success -> emit(Resource.Success(res.data.toHotelSearchResponse()))
            is Resource.Error -> emit(res)
            Resource.Loading -> Unit
        }
    }.retryWhen { cause, attempt ->
        val retryable = cause is IOException
        val http = (cause as? HttpFailureException)?.code
        val shouldRetry = retryable && attempt < 2 && http !in listOf(400, 404)
        if (shouldRetry) delay(backoffMillis(attempt))
        shouldRetry
    }

    fun clearCache() {
        featuredCache.value = emptyList()
        synchronized(lru) { lru.clear() }
    }
}
