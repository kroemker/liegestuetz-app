package com.liegestuetz.common

/**
 * A discriminated union representing the outcome of an operation that can succeed,
 * fail, or be in a loading state.
 *
 * Used throughout the app as the return type for repository and use-case calls
 * that cross layer boundaries (data → domain → presentation).
 */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: Throwable, val message: String? = null) : Result<Nothing>()
    data object Loading : Result<Nothing>()

    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    val isLoading: Boolean get() = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw exception
        is Loading -> error("Result is still Loading")
    }
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onError(action: (Throwable, String?) -> Unit): Result<T> {
    if (this is Result.Error) action(exception, message)
    return this
}

inline fun <T> Result<T>.onLoading(action: () -> Unit): Result<T> {
    if (this is Result.Loading) action()
    return this
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> = when (this) {
    is Result.Success -> Result.Success(transform(data))
    is Result.Error -> this
    is Result.Loading -> Result.Loading
}

inline fun <T> Result<T>.getOrElse(defaultValue: (Throwable) -> T): T = when (this) {
    is Result.Success -> data
    is Result.Error -> defaultValue(exception)
    is Result.Loading -> error("Result is still Loading")
}

/** Wraps a suspending block in a try/catch and returns Result.Success or Result.Error. */
suspend inline fun <T> runCatchingResult(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.Success(block())
    } catch (e: Exception) {
        Result.Error(e, e.message)
    }
