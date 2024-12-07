package com.sanisamoj.services.user

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.AccountType
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.utils.eraseAllDataInMongodb
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserServiceTest {
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()

    @AfterTest
    fun eraseAllUserData() {
        runBlocking { eraseAllDataInMongodb<User>(CollectionsInDb.Users) }
    }

    private fun validUserCreateRequest(): UserCreateRequest {
        return UserCreateRequest(
            nick = "user123",
            bio = "I love coding and testing",
            username = "user123",
            imageProfile = "https://example.com/profile.jpg",
            email = "user123@example.com",
            password = "securePassword123",
            phone = "+1234567890",
            type = AccountType.PARTICIPANT.name,
            preferences = UserPreference(eventPreferences = listOf("Technology")),
            doc = Doc(type = "passport", number = "123456789"),
            address = Address(
                city = "São Paulo",
                uf = "SP"
            )
        )
    }

    @Test
    fun createUser() = testApplication {
        val userService = UserService(repository)
        val userCreateRequest: UserCreateRequest = validUserCreateRequest()

        val userResponse: UserResponse = userService.createUser(userCreateRequest)

        assertEquals(userCreateRequest.nick, userResponse.nick)
        assertEquals(userCreateRequest.bio, userResponse.bio)
        assertEquals(userCreateRequest.username, userResponse.username)
        assertEquals(userCreateRequest.imageProfile, userResponse.imageProfile)
        assertEquals(userCreateRequest.email, userResponse.email)
        assertEquals(userCreateRequest.phone, userResponse.phone)
        assertEquals(userCreateRequest.type, userResponse.type)
        assertEquals(userCreateRequest.address, userResponse.address)
        assertEquals(userCreateRequest.preferences, userResponse.preferences)
        assertEquals(0, userResponse.presences)
        assertEquals(0, userResponse.followers)
        assertEquals(0, userResponse.following)
    }

    @Test
    fun `createUser should throw UserAlreadyExists when email or phone is already in use`() = testApplication {
        val userService = UserService(repository)
        val userCreateRequest: UserCreateRequest = validUserCreateRequest()
        userService.createUser(userCreateRequest)

        val exception = assertFailsWith<CustomException> {
            userService.createUser(userCreateRequest)
        }

        assertEquals(Errors.UserAlreadyExists, exception.error)
    }

    @Test
    fun `createUser should throw UnableToComplete for invalid phone number`() = testApplication {
        val userService = UserService(repository)
        val request: UserCreateRequest = validUserCreateRequest().copy(phone = "invalidPhone")

        val exception = assertFailsWith<CustomException> {
            runBlocking { userService.createUser(request) }
        }

        assertEquals(Errors.UnableToComplete, exception.error)
    }

    @Test
    fun `createUser should throw DataIsMissing when required fields are empty`() = testApplication {
        val userService = UserService(repository)
        val request: UserCreateRequest = validUserCreateRequest().copy(email = "")

        val exception = assertFailsWith<IllegalArgumentException> {
            userService.createUser(request)
        }

        assertEquals(Errors.DataIsMissing.description, exception.message)
    }

    @Test
    fun `createUser should throw InvalidParameters for invalid type`() = testApplication {
        val userService = UserService(repository)
        val request: UserCreateRequest = validUserCreateRequest().copy(type = "INVALID_TYPE")

        val exception = assertFailsWith<CustomException> {
            userService.createUser(request)
        }

        assertEquals(Errors.InvalidParameters, exception.error)
    }

}