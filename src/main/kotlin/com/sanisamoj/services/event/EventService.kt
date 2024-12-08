package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.EventStatus
import com.sanisamoj.data.models.enums.EventType
import com.sanisamoj.data.models.enums.InsigniaCriteriaType
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.data.models.interfaces.InsigniaRepository
import com.sanisamoj.services.media.MediaService
import com.sanisamoj.utils.analyzers.isInEnum
import com.sanisamoj.utils.converters.converterStringToLocalDateTime
import com.sanisamoj.utils.pagination.PaginationResponse
import com.sanisamoj.utils.pagination.paginationMethod
import java.io.File
import java.time.LocalDateTime

class EventService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository(),
    private val insigniaObserver: InsigniaRepository = GlobalContext.getInsigniaObserver()
) {

    suspend fun createEvent(accountId: String, eventRequest: CreateEventRequest): EventResponse {
        val file: File = MediaService().getMedia(eventRequest.image)
        if(!file.exists()) throw CustomException(Errors.UnableToComplete)

        eventRequest.type.forEach {
            if(!it.isInEnum<EventType>()) throw CustomException(Errors.InvalidParameters)
        }

        val eventDate: LocalDateTime = converterStringToLocalDateTime(eventRequest.date)
        if (eventDate.isBefore(LocalDateTime.now())) {
            throw CustomException(Errors.TheEventDateCannotBeInThePast)
        }

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
        insigniaObserver.addPoints(accountId, InsigniaCriteriaType.Events, 1.0)
        return eventResponseFactory(event)
    }

    suspend fun deleteEvent(eventId: String, accountId: String) {
        val event: Event = eventRepository.getEventById(eventId)
        if(event.accountId != accountId) throw CustomException(Errors.TheEventHasAnotherOwner)

        val allPresences: List<Presence> = eventRepository.getAllPresencesFromTheEvent(eventId)
        allPresences.forEach { presence ->
            try {
                eventRepository.unmarkPresence(presence.userId, eventId)
            } catch (_: Throwable) {}
        }

        eventRepository.deleteEvent(eventId)
        insigniaObserver.removePoints(accountId, InsigniaCriteriaType.Events, 1.0)

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

    suspend fun getPersonalizedEventFeed(userId: String, page: Int, size: Int): GenericResponseWithPagination<EventResponse> {
        val user: User = repository.getUserById(userId)

        val preferences: UserPreference = user.preferences
        val address: Address = user.address
        val filters = SearchEventFilters(
            address = AddressToSearch(
                city = address.city,
                uf = address.uf
            ),
            type = preferences.eventPreferences,
            status = EventStatus.SCHEDULED.name,
            endDate = LocalDateTime.now().plusDays(30),
            page = page,
            size = size
        )

        val eventList: List<Event> = eventRepository.searchEvents(filters)
        val eventsCount: Int = eventRepository.getEventsWithFilterCount(filters)
        val paginationResponse: PaginationResponse = paginationMethod(eventsCount.toDouble(), filters.size, filters.page)

        return GenericResponseWithPagination(
            content = eventList.map { eventResponseFactory(it) },
            paginationResponse = paginationResponse
        )
    }

    suspend fun getPersonalizedEventFeedWithInteractions(userId: String): List<EventResponse> {
        val user: User = repository.getUserById(userId)
        val followers: List<String> = repository.getFollowers(userId)
        val following: List<String> = repository.getFollowing(userId)
        val eventsFromFollowers: MutableList<Event> = mutableListOf()
        val eventsWithFollowerPresence: MutableList<Event> = mutableListOf()
        val creatorsEvents: MutableList<Event> = mutableListOf()

        followers.forEach { followerId ->
            val eventList = filterEvents(eventRepository.getAllEventFromAccount(followerId))
            eventsFromFollowers.addAll(eventList)
        }

        followers.forEach { followerId ->
            val presenceList = eventRepository.getAllPresenceByUser(followerId)
            presenceList.forEach { presence ->
                val event = eventRepository.getEventById(presence.eventId)
                if (event.status == EventStatus.SCHEDULED.name || event.status == EventStatus.ONGOING.name) {
                    eventsWithFollowerPresence.add(event)
                }
            }
        }

        following.forEach { followerId ->
            val eventList = filterEvents(eventRepository.getAllEventFromAccount(followerId))
            eventsFromFollowers.addAll(eventList)
        }

        following.forEach { followingId ->
            val presenceList = eventRepository.getAllPresenceByUser(followingId)
            presenceList.forEach { presence ->
                val event = eventRepository.getEventById(presence.eventId)
                if (event.status == EventStatus.SCHEDULED.name || event.status == EventStatus.ONGOING.name) {
                    eventsWithFollowerPresence.add(event)
                }
            }
        }

        user.preferences.creators.forEach { creatorId ->
            val creatorEvents = filterEvents(eventRepository.getAllEventFromAccount(creatorId))
            creatorsEvents.addAll(creatorEvents)
        }

        val combinedEvents: List<Event> = (eventsFromFollowers + eventsWithFollowerPresence + creatorsEvents).distinctBy { it.id }
        return combinedEvents.map { eventResponseFactory(it) }
    }

    private fun filterEvents(events: List<Event>): List<Event> {
        return events.filter { event ->
            event.status == EventStatus.SCHEDULED.name || event.status == EventStatus.ONGOING.name
        }
    }

    suspend fun eventResponseFactory(event: Event): EventResponse {
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