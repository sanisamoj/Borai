package com.sanisamoj.services.user

import com.sanisamoj.data.models.dataclass.*
import org.mindrot.jbcrypt.BCrypt
import kotlin.String

object UserFactory {
    fun userResponse(user: User): UserResponse {
        return UserResponse(
            id = user.id.toString(),
            username = user.username,
            imageProfile = if(user.imageProfile == "") null else user.imageProfile,
            email = user.email,
            phone = user.phone,
            createdAt = user.createdAt.toString(),
        )
    }

    // Transforms the user creation request into USER
    fun user(userCreateRequest: UserCreateRequest): User {
        val hashedPassword: String = BCrypt.hashpw(userCreateRequest.password, BCrypt.gensalt())
        return User(
            username = userCreateRequest.username,
            imageProfile = userCreateRequest.imageProfile ?: "",
            email = userCreateRequest.email,
            phone = userCreateRequest.phone,
            password = hashedPassword,
        )
    }
}