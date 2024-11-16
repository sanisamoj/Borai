package com.sanisamoj.api.bot

data class MessageToSend(
    val phone: String,
    val message: String,
    val imageUrl: String? = null
)
