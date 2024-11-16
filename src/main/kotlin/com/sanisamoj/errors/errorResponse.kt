package com.sanisamoj.errors

import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.ErrorResponse
import com.sanisamoj.data.models.enums.Errors
import io.ktor.http.HttpStatusCode

fun errorResponse(exception: CustomException): Pair<HttpStatusCode, ErrorResponse> {
    val response = when (exception.error) {
        Errors.UserAlreadyExists -> {
            HttpStatusCode.Conflict to ErrorResponse(Errors.UserAlreadyExists.description)
        }

        Errors.DataIsMissing -> {
            HttpStatusCode.BadRequest to ErrorResponse(Errors.DataIsMissing.description)
        }

        Errors.UserNotFound -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.UserNotFound.description)
        }

        else -> {
            HttpStatusCode.InternalServerError to ErrorResponse(
                error = Errors.InternalServerError.description,
                details = exception.additionalInfo
            )
        }
    }

    return response
}