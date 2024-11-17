package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.Presence
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository

class EventHandlerService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository(),
) {

    suspend fun markPresence(userId: String, eventId: String) {
        val presenceAlreadyExist: Presence? = eventRepository.getPresenceByEventAndUser(eventId, userId)
        if(presenceAlreadyExist != null) throw CustomException(Errors.PresenceAlreadyMarked)

        val user: User = repository.getUserById(userId)
        val presence = Presence(
            eventId = eventId,
            userId = userId,
            accountIsPublic = user.public
        )

        eventRepository.markPresencePresence(presence)
    }

    suspend fun unmaskPresence(userId: String, eventId: String) {
        val presenceAlreadyExist: Presence? = eventRepository.getPresenceByEventAndUser(eventId, userId)
        if(presenceAlreadyExist == null) throw CustomException(Errors.UnableToComplete)
        eventRepository.unmarkPresencePresence(userId, eventId)
    }

    suspend fun getPresenceByUser(userId: String, pageSize: Int = 10, pageNumber: Int = 1) {

    }

}