package com.sanisamoj.data.repository

import com.sanisamoj.api.bot.BotApi
import com.sanisamoj.api.bot.BotApiService
import com.sanisamoj.api.bot.LoginRequest
import com.sanisamoj.api.bot.MessageIdResponse
import com.sanisamoj.api.bot.MessageToSend
import com.sanisamoj.api.bot.MessageToSendWithImageUrl
import com.sanisamoj.api.log.EventSeverity
import com.sanisamoj.api.log.EventType
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.Infos
import com.sanisamoj.errors.LogFactory
import com.sanisamoj.errors.Logger
import com.sanisamoj.utils.analyzers.dotEnv
import java.lang.Thread.sleep
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object WhatsappBotRepository {
    private val email: String by lazy { dotEnv("BOT_LOGIN_EMAIL") }
    private val password: String by lazy { dotEnv("BOT_LOGIN_PASSWORD") }
    private val botApiService: BotApiService by lazy { BotApi.retrofitBotService }
    private lateinit var token: String
    private val maxRetries = 7

    suspend fun updateToken() {
        var attempts = 0
        while (attempts < maxRetries) {
            try {
                val loginRequest = LoginRequest(email, password)
                token = botApiService.login(loginRequest).token
                Logger.register(
                    log = LogFactory.log(
                        message = Infos.BotTokenUpdated.description,
                        eventType = EventType.INFO,
                        severity = EventSeverity.LOW,
                        additionalData = mapOf("at" to "${LocalDateTime.now()}")
                    )
                )
                println(Infos.BotTokenUpdated.description)
                break
            } catch (cause: Throwable) {
                println(cause)
                attempts++
                println("${Errors.BotTokenNotUpdated.description} Retry in 30 seconds! Attempt $attempts/$maxRetries")
                if (attempts >= maxRetries) {
                    Logger.register(
                        log = LogFactory.log(
                            message = Errors.MaxRetriesReached.description,
                            eventType = EventType.ERROR,
                            severity = EventSeverity.HIGH,
                            additionalData = mapOf("at" to "${LocalDateTime.now()}")
                        )
                    )
                    break
                }
                sleep(TimeUnit.SECONDS.toMillis(30))
            }
        }
    }

    suspend fun sendMessage(botId: String, messageToSend: MessageToSend): String {
        val messageIdResponse: MessageIdResponse = botApiService.sendMessage(botId, messageToSend, "Bearer $token")
        return messageIdResponse.messageId
    }

    suspend fun sendMessageWithImageUrl(botId: String, messageToSend: MessageToSendWithImageUrl): String {
        val messageIdResponse: MessageIdResponse = botApiService.sendMessageWithImageUrl(botId, messageToSend, "Bearer $token")
        return messageIdResponse.messageId
    }
}