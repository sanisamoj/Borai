package com.sanisamoj.services.user

import com.sanisamoj.data.models.dataclass.MinimalUserResponse
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.dataclass.UserCreateRequest
import com.sanisamoj.data.models.dataclass.UserResponse
import com.sanisamoj.data.models.enums.AccountStatus
import org.mindrot.jbcrypt.BCrypt

object UserFactory {
    fun userResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id.toString(),
            nick = user.nick,
            bio = user.bio,
            username = user.username,
            imageProfile = if(user.imageProfile == "") null else user.imageProfile,
            email = user.email,
            phone = user.phone,
            type = user.type,
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
            type = userCreateRequest.type,
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