package com.sanisamoj.config

import com.sanisamoj.data.models.interfaces.BotRepository
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.MailRepository
import com.sanisamoj.data.models.interfaces.ServerContainer
import com.sanisamoj.data.models.interfaces.SessionRepository
import com.sanisamoj.data.repository.DefaultBotRepository
import com.sanisamoj.data.repository.DefaultMailRepository
import com.sanisamoj.data.repository.DefaultRepository
import com.sanisamoj.data.repository.DefaultSessionRepository
import com.sanisamoj.data.repository.WhatsappBotRepository

class DefaultServerContainer: ServerContainer {
    override val databaseRepository: DatabaseRepository by lazy { DefaultRepository() }
    override val sessionRepository: SessionRepository by lazy { DefaultSessionRepository() }
    override val botRepository: BotRepository by lazy { DefaultBotRepository(WhatsappBotRepository) }
    override val mailRepository: MailRepository by lazy { DefaultMailRepository }
}