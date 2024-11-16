package com.sanisamoj.errors

import com.sanisamoj.api.log.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.Infos
import com.sanisamoj.utils.analyzers.dotEnv
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object Logger {
    private val repository: LogApiService by lazy { LogApi.retrofitLogService }
    private val applicationName: String = dotEnv("APP_LOG_NAME")
    private val password: String = dotEnv("APP_LOG_PASSWORD")
    private var token: String = ""
    private val maxRetries = 7

    suspend fun updateToken() {
        var attempts = 0
        while (attempts < maxRetries) {
            try {
                token = repository.applicationLogin(ApplicationServiceLoginRequest(applicationName, password)).token
                register(
                    log = LogFactory.log(
                        message = Infos.LogTokenUpdated.description,
                        eventType = EventType.INFO,
                        severity = EventSeverity.LOW,
                        additionalData = mapOf("at" to "${LocalDateTime.now()}")
                    )
                )
                println(Infos.LogTokenUpdated.description)
                return

            } catch (_: Throwable) {
                attempts++
                println("${Errors.LogTokenNotUpdated.description} Retry in 1 minute! Attempt $attempts/$maxRetries")

                if(attempts >= maxRetries) break
                delay(TimeUnit.SECONDS.toMillis(60))
            }
        }

    }

    suspend fun register(log: Log) {
        try {
            repository.registerLog(log, "Bearer $token")
        } catch (_: Throwable) {}
    }
}