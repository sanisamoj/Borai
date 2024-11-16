package com.sanisamoj.api.bot

import kotlinx.serialization.Serializable

@Serializable
data class MessageIdResponse(
    val messageId: String
)

