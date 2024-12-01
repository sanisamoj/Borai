package com.sanisamoj.services.user

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.services.insignia.InsigniaFactory
import com.sanisamoj.utils.pagination.PaginationResponse
import com.sanisamoj.utils.pagination.paginationMethod

class UserActivityService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {

    suspend fun getProfileById(profileId: String): ProfileResponse {
        val user: User = repository.getUserById(profileId)
        return profileResponseFactory(user)
    }

    suspend fun getProfilesByNick(nick: String): List<ProfileResponse> {
        val usersList: List<User> = repository.getUsersByNick(nick)
        return usersList.map { profileResponseFactory(it) }
    }

    private suspend fun profileResponseFactory(user: User): ProfileResponse {
        val userId: String = user.id.toString()
        val presencesCount: Int = eventRepository.getPresenceByUserCount(userId)
        val followers: Int = repository.getFollowers(userId).size
        val following: Int = repository.getFollowing(userId).size

        return ProfileResponse(
            id = userId,
            nick = user.nick,
            bio = user.bio,
            imageProfile = user.imageProfile,
            type = user.type,
            public = user.public,
            presences = presencesCount,
            followers = followers,
            following = following,
            visibleInsignias = user.visibleInsignias?.map { InsigniaFactory.insigniaResponseFactory(it) }
        )
    }

    suspend fun getPresencesFromProfile(userId: String, profileId: String, page: Int, size: Int): GenericResponseWithPagination<MinimalEventResponse> {
        val mutual: List<String> = repository.getMutualFollowers(profileId)
        val user: User = repository.getUserById(profileId)
        if(!mutual.contains(userId) && !user.public) throw CustomException(Errors.ProfileIsPrivate)

        return UserHandlerService().getPresenceByUser(profileId, size, page)
    }

    suspend fun getEventsFromProfile(userId: String, profileId: String, pageSize: Int, pageNumber: Int): GenericResponseWithPagination<MinimalEventResponse> {
        val mutual: List<String> = repository.getMutualFollowers(profileId)
        val user: User = repository.getUserById(profileId)
        if(!mutual.contains(userId) && !user.public) throw CustomException(Errors.ProfileIsPrivate)

        val events: List<Event> = eventRepository.getAllEventFromAccountWithPagination(profileId, pageNumber, pageSize)
        val eventResponseList: List<MinimalEventResponse> = events.map { minimalEventResponseFactory(it) }

        val eventsCount: Int = eventRepository.getAllEventFromAccountCount(profileId)
        val paginationResponse: PaginationResponse = paginationMethod(eventsCount.toDouble(), pageSize, pageNumber)

        return GenericResponseWithPagination(eventResponseList, paginationResponse)
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