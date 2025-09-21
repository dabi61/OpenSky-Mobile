package com.dabi.opensky.core.data.remote

import kotlinx.coroutines.CancellationException
import retrofit2.Response

sealed interface Resource<out T> {
    data object Loading : Resource<Nothing>
    data class Success<T>(val data: T) : Resource<T>
    data class Error(val cause: Throwable) : Resource<Nothing>
}

class EmptyBodyException : IllegalStateException("Empty body")
class HttpFailureException(val code: Int, message: String?) : RuntimeException("HTTP $code: $message")

suspend inline fun <T> apiCall(crossinline block: suspend () -> Response<T>): Resource<T> =
    try {
        val res = block()
        if (res.isSuccessful) {
            val body = res.body() ?: return Resource.Error(EmptyBodyException())
            Resource.Success(body)
        } else {
            Resource.Error(HttpFailureException(res.code(), res.message()))
        }
    } catch (ce: CancellationException) {
        throw ce
    } catch (t: Throwable) {
        Resource.Error(t)
    }

// A small exponential backoff utility
fun backoffMillis(attempt: Long, base: Long = 500L): Long =
    (1L shl attempt.toInt()) * base