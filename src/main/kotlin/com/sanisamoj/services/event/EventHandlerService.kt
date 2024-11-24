package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.utils.pagination.PaginationResponse
import com.sanisamoj.utils.pagination.paginationMethod

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
            nick = user.nick,
            imageProfile = user.imageProfile,
            accountIsPublic = user.public,
            accountType = user.type
        )

        eventRepository.markPresence(presence)
    }

    suspend fun unmaskPresence(userId: String, eventId: String) {
        val presenceAlreadyExist: Presence? = eventRepository.getPresenceByEventAndUser(eventId, userId)
        if(presenceAlreadyExist == null) throw CustomException(Errors.UnableToComplete)
        eventRepository.unmarkPresence(userId, eventId)
    }

    suspend fun getPublicPresencesFromTheEvent(eventId: String, pageNumber: Int = 1, pageSize: Int = 10): GenericResponseWithPagination<MinimalUserResponse> {
        val presences: List<Presence> = eventRepository.getPublicPresencesFromTheEvent(eventId, pageSize, pageNumber)
        val eventPresenceResponseList: List<MinimalUserResponse> = presences.map { it -> presenceResponseFactory(it) }

        val allPublicPresenceCount: Int = eventRepository.getPublicPresencesFromTheEventCount(eventId)
        val paginationResponse: PaginationResponse = paginationMethod(allPublicPresenceCount.toDouble(), pageSize, pageNumber)

        return GenericResponseWithPagination(content = eventPresenceResponseList, paginationResponse = paginationResponse)
    }

    suspend fun getMutualFollowersPresences(eventId: String, userId: String): List<MinimalUserResponse> {
        val allPresences: List<Presence> = eventRepository.getAllPublicPresencesFromTheEvent(eventId)
        val allPresencesInString: List<String> = allPresences.map { it.userId }
        val mutualFollowers: List<String> = repository.getMutualFollowers(userId)

        val minimalUserResponseList: MutableList<MinimalUserResponse> = mutableListOf()

        mutualFollowers.forEach { followerId ->
            if(allPresencesInString.contains(followerId)) {
                val presence: Presence = allPresences.find { it.userId == followerId }!!
                minimalUserResponseList.add(presenceResponseFactory(presence))
            }
        }

        return minimalUserResponseList
    }

    private fun presenceResponseFactory(presence: Presence): MinimalUserResponse {
        return MinimalUserResponse(
            id = presence.userId,
            nick = presence.nick,
            imageProfile = presence.imageProfile,
            accountType = presence.accountType
        )
    }

}