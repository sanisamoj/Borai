package com.sanisamoj.config

import com.sanisamoj.data.models.interfaces.*
import com.sanisamoj.data.repository.*
import com.sanisamoj.repository.TestBotRepository
import com.sanisamoj.repository.TestMailRepository

class ServerContainerTest: ServerContainer {
    override val databaseRepository: DatabaseRepository by lazy { DefaultRepository() }
    override val sessionRepository: SessionRepository by lazy { DefaultSessionRepository() }
    override val eventRepository: EventRepository by lazy { DefaultEventRepository() }
    override val botRepository: BotRepository by lazy { TestBotRepository() }
    override val mailRepository: MailRepository by lazy { TestMailRepository() }
    override val insigniaRepository: InsigniaRepository by lazy { InsigniaObserver }
}