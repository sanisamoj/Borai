package com.sanisamoj.config

import com.sanisamoj.data.models.interfaces.*

object GlobalContextTest {
    private val serverContainer: ServerContainer = ServerContainerTest()

    fun getDatabaseRepository(): DatabaseRepository = serverContainer.databaseRepository
    fun getSessionRepository(): SessionRepository = serverContainer.sessionRepository
    fun getEventRepository(): EventRepository = serverContainer.eventRepository
    fun getBotRepository(): BotRepository = serverContainer.botRepository
    fun getMailRepository(): MailRepository = serverContainer.mailRepository
    fun getInsigniaObserver(): InsigniaRepository = serverContainer.insigniaRepository
}