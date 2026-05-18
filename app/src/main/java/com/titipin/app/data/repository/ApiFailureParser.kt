package com.titipin.app.data.repository

import com.titipin.app.data.model.ApiResponse
import org.json.JSONObject
import retrofit2.Response

fun <T> Response<ApiResponse<T>>.apiFailure(defaultMessage: String): ApiFailure {
    body()?.let { apiResponse ->
        val message = apiResponse.error?.message
            ?: apiResponse.message
            ?: defaultMessage
        return ApiFailure(
            message = message,
            code = apiResponse.error?.code,
            httpCode = code(),
            errors = apiResponse.errors?.toString()
        )
    }

    val raw = errorBody()?.string().orEmpty()
    if (raw.isBlank()) {
        return ApiFailure(defaultMessage, httpCode = code())
    }

    return runCatching {
        val json = JSONObject(raw)
        val error = json.optJSONObject("error")
        val message = error?.optString("message")?.takeIf { it.isNotBlank() }
            ?: json.optString("message").takeIf { it.isNotBlank() }
            ?: defaultMessage
        val errorCode = error?.optString("code")?.takeIf { it.isNotBlank() }
        val errors = json.opt("errors")?.takeIf { it != JSONObject.NULL }?.toString()

        ApiFailure(
            message = message,
            code = errorCode,
            httpCode = code(),
            errors = errors
        )
    }.getOrElse {
        ApiFailure(defaultMessage, httpCode = code(), errors = raw.takeIf { it.isNotBlank() })
    }
}

fun <T> Response<ApiResponse<T>>.toResultError(defaultMessage: String): Result.Error {
    val failure = apiFailure(defaultMessage)
    return Result.Error(
        message = failure.message,
        code = failure.code,
        httpCode = failure.httpCode,
        errors = failure.errors
    )
}
