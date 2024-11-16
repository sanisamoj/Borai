package com.sanisamoj.api.bot

import kotlinx.serialization.Serializable

@Serializable
data class TokenResponse(
    val token: String
)