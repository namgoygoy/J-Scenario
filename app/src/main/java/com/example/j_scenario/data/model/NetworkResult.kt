package com.example.j_scenario.data.model

/**
 * Network API 호출 결과를 나타내는 sealed class
 */
sealed class NetworkResult<out T> {
    /**
     * 성공 상태
     */
    data class Success<T>(val data: T) : NetworkResult<T>()
    
    /**
     * 에러 상태
     */
    data class Error(
        val exception: Throwable,
        val message: String = exception.message ?: "알 수 없는 오류가 발생했습니다"
    ) : NetworkResult<Nothing>()
    
    /**
     * 로딩 상태
     */
    data object Loading : NetworkResult<Nothing>()
}

/**
 * NetworkResult 확장 함수들
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> NetworkResult<T>.onError(action: (Throwable) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(exception)
    }
    return this
}

inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) {
        action()
    }
    return this
}

