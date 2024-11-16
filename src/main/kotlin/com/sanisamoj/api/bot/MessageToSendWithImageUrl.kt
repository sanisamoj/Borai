package com.sanisamoj.api.bot

data class MessageToSendWithImageUrl(
    val to: String,
    val message: String? = null,
    val imageUrl: String
)

