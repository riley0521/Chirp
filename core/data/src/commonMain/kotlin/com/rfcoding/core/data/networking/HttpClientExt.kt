package com.rfcoding.core.data.networking

import com.rfcoding.core.data.BuildKonfig
import com.rfcoding.core.domain.util.DataError
import com.rfcoding.core.domain.util.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse

expect suspend fun <T> platformSafeCall(
    execute: suspend () -> HttpResponse,
    handleResponse: suspend (HttpResponse) -> Result<T, DataError.Remote>
): Result<T, DataError.Remote>

suspend inline fun <reified Request, reified Response: Any> HttpClient.post(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, DataError.Remote> {
    return safeCall {
        post {
            url(createRoute(route))
            queryParams.forEach { (key, value) ->
                parameter(key, value)
            }
            setBody(body)
            builder()
        }
    }
}

suspend inline fun <reified Request, reified Response: Any> HttpClient.put(
    route: String,
    body: Request,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, DataError.Remote> {
    return safeCall {
        put {
            url(createRoute(route))
            queryParams.forEach { (key, value) ->
                parameter(key, value)
            }
            setBody(body)
            builder()
        }
    }
}

suspend inline fun <reified Response: Any> HttpClient.get(
    route: String,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, DataError.Remote> {
    return safeCall {
        get {
            url(createRoute(route))
            queryParams.forEach { (key, value) ->
                parameter(key, value)
            }
            builder()
        }
    }
}

suspend inline fun <reified Response: Any> HttpClient.delete(
    route: String,
    queryParams: Map<String, Any> = mapOf(),
    crossinline builder: HttpRequestBuilder.() -> Unit = {}
): Result<Response, DataError.Remote> {
    return safeCall {
        delete {
            url(createRoute(route))
            queryParams.forEach { (key, value) ->
                parameter(key, value)
            }
            builder()
        }
    }
}

suspend inline fun <reified T> safeCall(
    noinline execute: suspend () -> HttpResponse
): Result<T, DataError.Remote> {
    return platformSafeCall(
        execute = execute
    ) { response ->
        responseToResult(response)
    }
}

suspend inline fun <reified T> responseToResult(response: HttpResponse): Result<T, DataError.Remote> {
    return when (response.status.value) {
        in 200..299 -> {
            try {
                Result.Success(response.body<T>())
            } catch (e: NoTransformationFoundException) {
                Result.Failure(DataError.Remote.SERIALIZATION)
            }
        }
        400 -> Result.Failure(DataError.Remote.BAD_REQUEST, response.body<ErrorDto>().message)
        401 -> Result.Failure(DataError.Remote.UNAUTHORIZED, response.body<ErrorDto>().message)
        403 -> Result.Failure(DataError.Remote.FORBIDDEN, response.body<ErrorDto>().message)
        404 -> Result.Failure(DataError.Remote.NOT_FOUND, response.body<ErrorDto>().message)
        408 -> Result.Failure(DataError.Remote.REQUEST_TIMEOUT, response.body<ErrorDto>().message)
        409 -> Result.Failure(DataError.Remote.CONFLICT, response.body<ErrorDto>().message)
        413 -> Result.Failure(DataError.Remote.PAYLOAD_TOO_LARGE, response.body<ErrorDto>().message)
        429 -> Result.Failure(DataError.Remote.TOO_MANY_REQUESTS, response.body<ErrorDto>().message)
        500 -> Result.Failure(DataError.Remote.SERVER_ERROR, response.body<ErrorDto>().message)
        503 -> Result.Failure(DataError.Remote.SERVICE_UNAVAILABLE, response.body<ErrorDto>().message)
        else -> Result.Failure(DataError.Remote.UNKNOWN, response.body<ErrorDto>().message)
    }
}

fun createRoute(route: String): String {
    return when {
        route.contains(BuildKonfig.BASE_URL) -> route
        route.startsWith("/") -> "${BuildKonfig.BASE_URL}$route"
        else -> "${BuildKonfig.BASE_URL}/$route"
    }
}