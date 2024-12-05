package com.sanisamoj.repository

import com.sanisamoj.api.bot.MessageToSend
import com.sanisamoj.api.bot.MessageToSendWithImageUrl
import com.sanisamoj.data.models.interfaces.BotRepository

class TestBotRepository: BotRepository {
    override suspend fun sendMessage(botId: String, messageToSend: MessageToSend): String {
        return (0..9999).random().toString()
    }

    override suspend fun sendMessageWithImageUrl(botId: String, messageToSend: MessageToSendWithImageUrl): String {
        return (0..9999).random().toString()
    }
}