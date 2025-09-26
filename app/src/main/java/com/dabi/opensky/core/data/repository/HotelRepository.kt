package com.dabi.opensky.core.data.repository

import com.dabi.opensky.core.data.remote.HttpFailureException
import com.dabi.opensky.core.data.remote.Resource
import com.dabi.opensky.core.data.remote.api.HotelService
import com.dabi.opensky.core.data.remote.api.RoomService
import com.dabi.opensky.core.data.remote.apiCall
import com.dabi.opensky.core.data.remote.backoffMillis
import com.dabi.opensky.core.model.hotel.Hotel
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

    // ---- Search LRU cache with TTL ----
    private data class CacheEntry(
        val data: HotelSearchResponse,
        val timestamp: Long
    )

    private val ttlMillis = 5 * 60 * 1000L // 5 minutes
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

    private fun keyOf(r: HotelSearchRequest): String = listOf(
        r.q, r.province, r.address, r.stars, r.minPrice, r.maxPrice, r.sortBy, r.sortOrder, r.page, r.limit
    ).joinToString("|") { it?.toString().orEmpty() }

// ---- Public APIs ----

    fun getFeaturedHotels(limit: Int = 5): Flow<Resource<List<Hotel>>> = flow {
// Emit cache first if any
        val cached = featuredCache.value
        if (cached.isNotEmpty()) emit(Resource.Success(cached))
        emit(Resource.Loading)

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

    fun getHotelById(id: String): Flow<Resource<Hotel>> = flow {
        println("HotelRepository: Getting hotel by ID: $id")
        emit(Resource.Loading)

        // Try primary API endpoint first
        var result = apiCall {
            println("HotelRepository: Making API call to /hotels/$id")
            hotelApi.getHotelById(id)
        }

        // If primary endpoint fails with 404, try alternative endpoint
        if (result is Resource.Error &&
            result.cause is HttpFailureException &&
            result.cause.code == 404
        ) {

            println("HotelRepository: Primary endpoint failed, trying alternative /hotel/$id")
            result = apiCall {
                hotelApi.getHotelById(id)
            }
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

                    // If 404, try to find hotel in search cache as fallback
                    if (result.cause.code == 404) {
                        println("HotelRepository: Trying fallback - searching in cache for hotel ID: $id")

                        // Look through search cache for this hotel
                        val cachedHotel = synchronized(lru) {
                            lru.values.flatMap { it.data.hotels }.find { it.hotelID == id }
                        }

                        if (cachedHotel != null) {
                            println("HotelRepository: Found hotel in cache: ${cachedHotel.hotelName}")
                            emit(Resource.Success(cachedHotel))
                        } else {
                            // Try searching for all hotels and find the one with matching ID
                            println("HotelRepository: Hotel not in cache, trying to search all hotels...")
                            val searchResult =
                                apiCall { hotelApi.getAllHotels(page = 1, limit = 100) }

                            when (searchResult) {
                                is Resource.Success -> {
                                    val foundHotel =
                                        searchResult.data.hotels.find { it.hotelID == id }
                                    if (foundHotel != null) {
                                        println("HotelRepository: Found hotel in all hotels: ${foundHotel.hotelName}")
                                        emit(Resource.Success(foundHotel))
                                    } else {
                                        println("HotelRepository: Hotel not found anywhere")
                                        emit(result) // Original error
                                    }
                                }

                                is Resource.Error -> {
                                    println("HotelRepository: Fallback search also failed")
                                    emit(result) // Original error
                                }

                                is Resource.Loading -> emit(result)
                            }
                        }
                    } else {
                        emit(result)
                    }
                } else {
                    emit(result)
                }
            }

            is Resource.Loading -> {
                println("HotelRepository: Still loading...")
                emit(result)
            }
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
fun getAllHotels(
    page: Int = 1,
    limit: Int = 10,
    sortBy: String = "name",
    sortOrder: String = "asc"
): Flow<Resource<HotelSearchResponse>> = flow {
    emit(Resource.Loading)
    emit(apiCall { hotelApi.getAllHotels(page, limit, sortBy, sortOrder) })
}.retryWhen { cause, attempt ->
    val retryable = cause is IOException
    val http = (cause as? HttpFailureException)?.code
    val shouldRetry = retryable && attempt < 3 && http !in listOf(400, 404)
    if (shouldRetry) delay(backoffMillis(attempt))
    shouldRetry
}

fun searchHotels(request: HotelSearchRequest): Flow<Resource<HotelSearchResponse>> = flow {
    emit(Resource.Loading)
    clearExpired()
    val key = keyOf(request)

    cacheGet(key)?.takeIf { !isExpired(it.timestamp) }?.let {
        emit(Resource.Success(it.data))
        return@flow
    }

    when (val res = apiCall {
        hotelApi.searchHotels(
            query = request.q,
            province = request.province,
            address = request.address,
            stars = request.stars,
            minPrice = request.minPrice,
            maxPrice = request.maxPrice,
            sortBy = request.sortBy,
            sortOrder = request.sortOrder,
            page = request.page,
            limit = request.limit
        )
    }) {
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
 * Get hotels by province ID với API mới
 */
fun getHotelsByProvinceId(
    provinceId: Int,
    page: Int = 1,
    size: Int = 10
): Flow<Resource<HotelSearchResponse>> = flow {
    emit(Resource.Loading)

    when (val res = apiCall { hotelApi.getHotelsByProvinceId(provinceId, page, size) }) {
        is Resource.Success -> {
            // Convert HotelsByProvinceResponse to HotelSearchResponse
            emit(Resource.Success(res.data.toHotelSearchResponse()))
        }

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
 * Get hotels by province name (backward compatibility)
 */
fun getHotelsByProvince(
    province: String,
    page: Int = 1,
    limit: Int = 10
): Flow<Resource<HotelSearchResponse>> = flow {
    emit(Resource.Loading)
    emit(apiCall { hotelApi.getHotelsByProvince(province, page, limit) })
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