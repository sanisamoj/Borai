package com.sanisamoj.utils.generators

data class TokenInfo(
    val id: String,
    val email: String,
    val sessionId: String,
    val secret: String,
    val time: Long
)
