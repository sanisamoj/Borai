package com.sanisamoj.services.user

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.GenericResponseWithPagination
import com.sanisamoj.data.models.dataclass.MinimalEventResponse
import com.sanisamoj.data.models.dataclass.ProfileResponse
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository

class UserActivityService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {
    suspend fun getProfileById(profileId: String): ProfileResponse {
        val user: User = repository.getUserById(profileId)
        val presencesCount: Int = eventRepository.getPresenceByUserCount(profileId)
        val followers: Int = repository.getFollowers(profileId).size
        val following: Int = repository.getFollowing(profileId).size

        return ProfileResponse(
            id = user.id.toString(),
            nick = user.nick,
            bio = user.bio,
            imageProfile = user.imageProfile,
            type = user.type,
            public = user.public,
            events = presencesCount,
            followers = followers,
            following = following
        )
    }

    suspend fun getPresencesFromProfile(userId: String, profileId: String, page: Int, size: Int): GenericResponseWithPagination<MinimalEventResponse> {
        val mutual: List<String> = repository.getMutualFollowers(profileId)
        val user: User = repository.getUserById(profileId)
        if(!mutual.contains(userId) && user.public == false) throw CustomException(Errors.ProfileIsPrivate)

        return UserHandlerService().getPresenceByUser(profileId, size, page)
    }
}