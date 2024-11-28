package com.sanisamoj.services.moderator

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.utils.pagination.PaginationResponse
import com.sanisamoj.utils.pagination.paginationMethod

class ModeratorActivityService(
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository(),
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
) {

    suspend fun getUsersWithPagination(page: Int, size: Int): GenericResponseWithPagination<UserForModeratorResponse> {
        val users: List<User> = repository.getUsersWithPagination(size, page)
        val getAllUsersCount: Int = repository.getUsersCount()
        val paginationResponse: PaginationResponse = paginationMethod(getAllUsersCount.toDouble(), size, page)
        val usersForModeratorResponseList: List<UserForModeratorResponse> = users.map { userForModeratorResponseFactory(it) }

        return GenericResponseWithPagination(
            content = usersForModeratorResponseList,
            paginationResponse = paginationResponse
        )

    }

    suspend fun deleteEvent(eventId: String) {
        val allPresences: List<Presence> = eventRepository.getAllPresencesFromTheEvent(eventId)
        allPresences.forEach { presence ->
            try {
                eventRepository.unmarkPresence(presence.userId, eventId)
            } catch (_: Throwable) {}
        }

        eventRepository.deleteEvent(eventId)
    }

    private suspend fun userForModeratorResponseFactory(user: User): UserForModeratorResponse {
        val events: List<Event> = eventRepository.getAllEventFromAccount(user.id.toString())
        val minimalEventResponseList: List<MinimalEventResponse> = events.map { minimalEventResponseFactory(it) }

        val eventHandlerService: List<Event> = eventRepository.getAllEventFromAccount(user.id.toString())
        val minimalUserResponseListForPresences: List<MinimalEventResponse> = eventHandlerService.map { minimalEventResponseFactory(it) }

        val userForModeratorResponse = UserForModeratorResponse(
            id = user.id.toString(),
            nick = user.nick,
            bio = user.bio,
            username = user.username,
            imageProfile = user.imageProfile,
            email = user.email,
            phone = user.phone,
            address = user.address,
            events = minimalEventResponseList,
            presences = minimalUserResponseListForPresences,
            createdAt = user.createdAt.toString()
        )

        return userForModeratorResponse

    }

    private fun minimalEventResponseFactory(event: Event): MinimalEventResponse {
        return MinimalEventResponse(
            id = event.id.toString(),
            name = event.name,
            description = event.description,
            image = event.image,
            presences = event.presences,
            type = event.type,
            date = event.date.toString()
        )
    }

}