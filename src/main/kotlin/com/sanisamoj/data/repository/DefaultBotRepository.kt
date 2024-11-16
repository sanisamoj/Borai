package com.sanisamoj.data.repository

import com.sanisamoj.api.bot.MessageToSend
import com.sanisamoj.api.bot.MessageToSendWithImageUrl
import com.sanisamoj.data.models.interfaces.BotRepository

class DefaultBotRepository(
    private val whatsappBotRepository: WhatsappBotRepository = WhatsappBotRepository
): BotRepository {
    override suspend fun sendMessage(botId: String, messageToSend: MessageToSend): String {
        return whatsappBotRepository.sendMessage(botId, messageToSend)
    }

    override suspend fun sendMessageWithImageUrl(botId: String, messageToSend: MessageToSendWithImageUrl): String {
        return whatsappBotRepository.sendMessageWithImageUrl(botId, messageToSend)
    }
}