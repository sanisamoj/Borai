package com.sanisamoj.data.models.interfaces

interface ServerContainer {
    val databaseRepository: DatabaseRepository
    val sessionRepository: SessionRepository
    val eventRepository: EventRepository
    val botRepository: BotRepository
    val mailRepository: MailRepository
    val insigniaRepository: InsigniaRepository
}