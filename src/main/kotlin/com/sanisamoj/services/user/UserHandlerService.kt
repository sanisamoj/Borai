package com.sanisamoj.services.user

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.utils.pagination.PaginationResponse
import com.sanisamoj.utils.pagination.paginationMethod

class UserHandlerService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {

    suspend fun getPresenceByUser(userId: String, pageSize: Int, pageNumber: Int): GenericResponseWithPagination<MinimalEventResponse> {
        val presences: List<Presence> = eventRepository.getPresenceByUser(userId, pageSize, pageNumber)
        val presencesCount: Double = eventRepository.getPresenceByUserCount(userId).toDouble()
        val paginationResponse: PaginationResponse = paginationMethod(presencesCount, pageSize, pageNumber)

        return GenericResponseWithPagination(
            content = presences.map { minimalEventResponseFactory(it.eventId) },
            paginationResponse = paginationResponse
        )
    }

    suspend fun getFollowers(userId: String, pageSize: Int, pageNumber: Int): GenericResponseWithPagination<MinimalUserResponse> {
        val followersList: List<String> = repository.getFollowers(userId)
        val paginationResponse: PaginationResponse = paginationMethod(followersList.size.toDouble(), pageSize, pageNumber)

        val startIndex: Int = (pageNumber - 1) * pageSize
        val endIndex: Int = (startIndex + pageSize).coerceAtMost(followersList.size)

        if (startIndex >= followersList.size || pageNumber <= 0) {
            return GenericResponseWithPagination(
                content = emptyList(),
                paginationResponse = paginationResponse
            )
        }

        val paginatedFollowers: List<String> = followersList.subList(startIndex, endIndex)
        val mappedFollowers: List<MinimalUserResponse> = paginatedFollowers.map { id ->
            val user: User = repository.getUserById(id)
            UserFactory.minimalUserResponseWithUser(user)
        }

        return GenericResponseWithPagination(
            content = mappedFollowers,
            paginationResponse = paginationResponse
        )
    }

    suspend fun getFollowing(userId: String, pageSize: Int, pageNumber: Int): GenericResponseWithPagination<MinimalUserResponse> {
        val followingList: List<String> = repository.getFollowing(userId)
        val paginationResponse: PaginationResponse = paginationMethod(followingList.size.toDouble(), pageSize, pageNumber)

        val startIndex: Int = (pageNumber - 1) * pageSize
        val endIndex: Int = (startIndex + pageSize).coerceAtMost(followingList.size)

        if (startIndex >= followingList.size || pageNumber <= 0) {
            return GenericResponseWithPagination(
                content = emptyList(),
                paginationResponse = paginationResponse
            )
        }

        val paginatedFollowing: List<String> = followingList.subList(startIndex, endIndex)
        val mappedFollowers: List<MinimalUserResponse> = paginatedFollowing.map { id ->
            val user: User = repository.getUserById(id)
            UserFactory.minimalUserResponseWithUser(user)
        }

        return GenericResponseWithPagination(
            content = mappedFollowers,
            paginationResponse = paginationResponse
        )
    }

    private suspend fun minimalEventResponseFactory(eventId: String): MinimalEventResponse {
        val event: Event = eventRepository.getEventById(eventId)
        return MinimalEventResponse(
            id = eventId,
            name = event.name,
            description = event.description,
            image = event.image,
            presences = event.presences,
            type = event.type,
            date = event.date.toString()
        )
    }

}