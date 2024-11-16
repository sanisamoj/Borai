package com.sanisamoj.pluguins

import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.ErrorResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.errors.LogFactory
import com.sanisamoj.errors.errorResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPage() {
    install(StatusPages) {
        exception<CustomException> { call, cause ->
            println(cause)
            println(LogFactory.getStackTraceAsString(cause))
            val response: Pair<HttpStatusCode, ErrorResponse> = errorResponse(cause)
            call.respond(response.first, response.second)
        }

        exception<Throwable> { call, cause ->
            println(cause)
            println(LogFactory.getStackTraceAsString(cause))
            val response: Pair<HttpStatusCode, ErrorResponse> = errorResponse(CustomException(Errors.InternalServerError))
            call.respond(response.first, response.second)
        }

        status(HttpStatusCode.TooManyRequests) { call, status ->
            val retryAfter = call.response.headers["Retry-After"]
            val errorResponse = ErrorResponse(Errors.TooManyRequests.description, "Wait for $retryAfter seconds.")
            call.respond(status, errorResponse)
        }
    }
}