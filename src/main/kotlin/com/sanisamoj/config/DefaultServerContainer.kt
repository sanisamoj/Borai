package com.sanisamoj.config

import com.sanisamoj.data.models.interfaces.*
import com.sanisamoj.data.repository.*

class DefaultServerContainer: ServerContainer {
    override val databaseRepository: DatabaseRepository by lazy { DefaultRepository() }
    override val sessionRepository: SessionRepository by lazy { DefaultSessionRepository() }
    override val eventRepository: EventRepository  by lazy { DefaultEventRepository() }
    override val botRepository: BotRepository by lazy { DefaultBotRepository(WhatsappBotRepository) }
    override val mailRepository: MailRepository by lazy { DefaultMailRepository }
    override val insigniaRepository: InsigniaRepository by lazy { InsigniaObserver }
}