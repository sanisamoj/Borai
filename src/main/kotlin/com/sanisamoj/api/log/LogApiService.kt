package com.sanisamoj.api.log

import com.sanisamoj.api.bot.TokenResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LogApiService {

    @POST("application/login")
    suspend fun applicationLogin(
        @Body loginRequest: ApplicationServiceLoginRequest
    ) : TokenResponse

    @POST("log")
    suspend fun registerLog(
        @Body log: Log,
        @Header("Authorization") token: String
    ) : LogResponse

}