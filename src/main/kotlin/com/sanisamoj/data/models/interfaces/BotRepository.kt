package com.sanisamoj.data.models.interfaces

import com.sanisamoj.api.bot.MessageToSend
import com.sanisamoj.api.bot.MessageToSendWithImageUrl

interface BotRepository {
    suspend fun sendMessage(botId: String, messageToSend: MessageToSend): String
    suspend fun sendMessageWithImageUrl(botId: String, messageToSend: MessageToSendWithImageUrl): String
}