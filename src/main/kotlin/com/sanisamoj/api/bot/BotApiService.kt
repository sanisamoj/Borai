package com.sanisamoj.api.bot

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface BotApiService {
    @POST("admin")
    suspend fun login(
        @Body loginRequest: LoginRequest
    ): TokenResponse

    @POST("bot/{id}/message")
    suspend fun sendMessage(
        @Path("id") botId: String,
        @Body message: MessageToSend,
        @Header("Authorization") token: String
    ) : MessageIdResponse

    @POST("bot/{id}/img-url-message")
    suspend fun sendMessageWithImageUrl(
        @Path("id") botId: String,
        @Body message: MessageToSendWithImageUrl,
        @Header("Authorization") token: String
    ) : MessageIdResponse
}