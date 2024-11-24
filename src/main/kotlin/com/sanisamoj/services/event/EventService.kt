package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.utils.converters.converterStringToLocalDateTime
import com.sanisamoj.utils.pagination.PaginationResponse
import com.sanisamoj.utils.pagination.paginationMethod

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

    suspend fun searchEvents(filters: SearchEventFilters): GenericResponseWithPagination<EventResponse> {
        val eventList: List<Event> = eventRepository.searchEvents(filters)
        val eventsCount: Int = eventRepository.getEventsWithFilterCount(filters)
        val paginationResponse: PaginationResponse = paginationMethod(eventsCount.toDouble(), filters.size, filters.page)

        return GenericResponseWithPagination(
            content = eventList.map { eventResponseFactory(it) },
            paginationResponse = paginationResponse
        )
    }

    suspend fun findEventsNearby(filters: SearchEventNearby): GenericResponseWithPagination<EventResponse> {
        val eventList: List<Event> = eventRepository.findEventsNearby(filters)
        val eventsCount: Int = eventRepository.getEventsWithFilterCount(filters)
        val paginationResponse: PaginationResponse = paginationMethod(eventsCount.toDouble(), filters.size, filters.page)

        return GenericResponseWithPagination(
            content = eventList.map { eventResponseFactory(it) },
            paginationResponse = paginationResponse
        )
    }

    private suspend fun eventResponseFactory(event: Event): EventResponse {
        val user: User = repository.getUserById(event.accountId)

        val minimalUserResponse = MinimalUserResponse(
            id = user.id.toString(),
            nick = user.nick,
            imageProfile = user.imageProfile,
            accountType = user.type
        )

        val eventResponse = EventResponse(
            id = event.id.toString(),
            eventCreator = minimalUserResponse,
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