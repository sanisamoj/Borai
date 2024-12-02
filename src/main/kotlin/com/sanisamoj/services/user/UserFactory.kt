package com.sanisamoj.services.user

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.AccountStatus
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.services.insignia.InsigniaFactory
import org.mindrot.jbcrypt.BCrypt

object UserFactory {
    private val eventRepository: EventRepository = GlobalContext.getEventRepository()
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()

    suspend fun userResponse(user: User): UserResponse {
        val userId: String = user.id.toString()
        val presencesCount: Int = eventRepository.getPresenceByUserCount(userId)
        val followers: Int = repository.getFollowers(userId).size
        val following: Int = repository.getFollowing(userId).size

        return UserResponse(
            id = user.id.toString(),
            nick = user.nick,
            bio = user.bio,
            username = user.username,
            imageProfile = if(user.imageProfile == "") null else user.imageProfile,
            email = user.email,
            phone = user.phone,
            type = user.type,
            presences = presencesCount,
            followers = followers,
            following = following,
            insignias = user.insignias?.map { InsigniaFactory.insigniaResponseFactory(it) },
            visibleInsignias = user.visibleInsignias?.map { InsigniaFactory.insigniaResponseFactory(it) },
            preferences = user.preferences,
            createdAt = user.createdAt.toString(),
        )
    }

    suspend fun userResponse(userId: String): UserResponse {
        val user: User = repository.getUserById(userId)
        val presencesCount: Int = eventRepository.getPresenceByUserCount(userId)
        val followers: Int = repository.getFollowers(userId).size
        val following: Int = repository.getFollowing(userId).size

        return UserResponse(
            id = user.id.toString(),
            nick = user.nick,
            bio = user.bio,
            username = user.username,
            imageProfile = if(user.imageProfile == "") null else user.imageProfile,
            email = user.email,
            phone = user.phone,
            type = user.type,
            presences = presencesCount,
            followers = followers,
            following = following,
            insignias = user.insignias?.map { InsigniaFactory.insigniaResponseFactory(it) },
            visibleInsignias = user.visibleInsignias?.map { InsigniaFactory.insigniaResponseFactory(it) },
            preferences = user.preferences,
            createdAt = user.createdAt.toString(),
        )
    }

    // Transforms the user creation request into USER
    fun user(userCreateRequest: UserCreateRequest): User {
        val hashedPassword: String = BCrypt.hashpw(userCreateRequest.password, BCrypt.gensalt())
        return User(
            nick = userCreateRequest.nick,
            bio = userCreateRequest.bio,
            username = userCreateRequest.username,
            imageProfile = userCreateRequest.imageProfile ?: "",
            email = userCreateRequest.email,
            phone = userCreateRequest.phone,
            password = hashedPassword,
            address = userCreateRequest.address,
            type = userCreateRequest.type,
            preferences = UserPreference(),
            accountStatus = AccountStatus.Inactive.name
        )
    }

    fun minimalUserResponseWithUser(user: User): MinimalUserResponse {
        return MinimalUserResponse(
            id = user.id.toString(),
            bio = user.bio,
            nick = user.nick,
            imageProfile = user.imageProfile,
            accountType = user.type
        )
    }
}