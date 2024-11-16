package com.sanisamoj.pluguins

import io.ktor.server.application.*
import io.ktor.server.plugins.ratelimit.*
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

fun Application.configureRateLimit() {
    install(RateLimit) {
        register(RateLimitName("register")) {
            rateLimiter(limit = 6, refillPeriod = 1.hours)
        }

        register(RateLimitName("validation")) {
            rateLimiter(limit = 10, refillPeriod = 24.hours)
        }

        register(RateLimitName("lightweight")) {
            rateLimiter(limit = 3, refillPeriod = 1.seconds)
        }

        register(RateLimitName("login")) {
            rateLimiter(limit = 8, refillPeriod = 1.hours)
        }
    }
}