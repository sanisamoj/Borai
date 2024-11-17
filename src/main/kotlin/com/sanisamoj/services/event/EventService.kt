package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.utils.converters.converterStringToLocalDateTime

class EventService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {

    suspend fun createEvent(accountId: String, eventRequest: CreateEventRequest): EventResponse {
        val event = Event(
            accountId = accountId,
            name = eventRequest.name,
            description = eventRequest.description,
            image = eventRequest.image,
            otherImages = eventRequest.otherImages,
            address = eventRequest.address,
            date = converterStringToLocalDateTime(eventRequest.date),
            type = eventRequest.type
        )

        eventRepository.createEvent(event)
        return eventResponseFactory(event)
    }

    suspend fun getEventById(eventId: String): EventResponse {
        val event: Event = eventRepository.getEventById(eventId)
        return eventResponseFactory(event)
    }

    suspend fun searchEvents(filters: SearchEventFilters): List<EventResponse> {
        val eventList: List<Event> = eventRepository.searchEvents(filters)
        return eventList.map { eventResponseFactory(it) }
    }

    suspend fun findEventsNearby(filters: SearchEventNearby): List<EventResponse> {
        val eventList: List<Event> = eventRepository.findEventsNearby(filters)
        return eventList.map { eventResponseFactory(it) }
    }

    private suspend fun eventResponseFactory(event: Event): EventResponse {
        val user: User = repository.getUserById(event.accountId)

        val eventCreatorResponse = EventCreatorResponse(
            id = user.id.toString(),
            nick = user.nick,
            imageProfile = user.imageProfile,
            accountType = user.type
        )

        val eventResponse = EventResponse(
            id = event.id.toString(),
            eventCreator = eventCreatorResponse,
            name = event.name,
            description = event.description,
            image = event.image,
            otherImages = event.otherImages,
            address = event.address,
            date = event.date.toString(),
            presences = event.presences,
            type = event.type,
            status = event.status,
            createdAt = event.createdAt.toString()
        )

        return eventResponse
    }

}