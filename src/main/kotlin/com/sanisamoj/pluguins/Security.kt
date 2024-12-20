package com.sanisamoj.pluguins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.SessionRepository
import com.sanisamoj.utils.analyzers.dotEnv
import io.ktor.server.application.*
import io.ktor.server.auth.authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureSecurity(
    sessionRepository: SessionRepository = GlobalContext.getSessionRepository()
) {
    val jwtAudience = dotEnv("JWT_AUDIENCE")
    val jwtDomain = dotEnv("JWT_DOMAIN")
    val userSecret = dotEnv("USER_SECRET")
    val moderatorSecret = dotEnv("MODERATOR_SECRET")

    authentication {
        jwt("user-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(userSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                val accountId = credential.payload.getClaim("id").asString()
                val sessionId = credential.payload.getClaim("session").asString()
                verifySession(
                    sessionRepository = sessionRepository,
                    accountId = accountId,
                    sessionId = sessionId
                )
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }

        jwt("moderator-jwt") {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(moderatorSecret))
                    .withAudience(jwtAudience)
                    .withIssuer(jwtDomain)
                    .build()
            )
            validate { credential ->
                val accountId = credential.payload.getClaim("id").asString()
                val sessionId = credential.payload.getClaim("session").asString()
                verifySession(
                    sessionRepository = sessionRepository,
                    accountId = accountId,
                    sessionId = sessionId
                )
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }
}

private suspend fun verifySession(sessionRepository: SessionRepository, accountId: String, sessionId: String) {
    val sessionRevoked: Boolean = sessionRepository.sessionRevoked(accountId, sessionId)
    if (sessionRevoked) throw CustomException(Errors.ExpiredSession)
}