package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.Address
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.Event
import com.sanisamoj.data.models.dataclass.EventResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.utils.converters.converterStringToLocalDateTime

class EventManagerService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {

    suspend fun updateName(eventId: String, userId: String, newName: String): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Name, newName))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateDescription(eventId: String, userId: String, newDescription: String): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Description, newDescription))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateAddress(eventId: String, userId: String, newAddress: Address): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Address, newAddress))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateDate(eventId: String, userId: String, newDate: String): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Date, converterStringToLocalDateTime(newDate)))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateType(eventId: String, userId: String, newTypes: List<String>): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Type, newTypes))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateStatus(eventId: String, userId: String, newStatus: String): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Status, newStatus))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    private suspend fun verifyIfAccountIsOwner(accountId: String, eventId: String) {
        val event: Event = eventRepository.getEventById(eventId)
        if(event.accountId != accountId) throw CustomException(Errors.TheEventHasAnotherOwner)
    }

}