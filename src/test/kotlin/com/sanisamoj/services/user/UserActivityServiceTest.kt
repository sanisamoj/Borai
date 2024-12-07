package com.sanisamoj.services.user

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.utils.eraseAllDataInMongodb
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserActivityServiceTest {
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()

    @AfterTest
    fun eraseAllUserData() {
        runBlocking { eraseAllDataInMongodb<User>(CollectionsInDb.Users) }
    }

    @Test
    fun getProfileByIdTest() = testApplication {
        val userResponse: UserResponse = UserRequestFactory.createUser()
        val userActivityService = UserActivityService(repository = repository)

        val profileResponse: ProfileResponse = userActivityService.getProfileById(userResponse.id)
        assertEquals(userResponse.id, profileResponse.id)
        assertEquals(userResponse.nick, profileResponse.nick)
        assertEquals(userResponse.bio, profileResponse.bio)
        assertEquals(userResponse.type, profileResponse.type)
        assertEquals(userResponse.presences, profileResponse.presences)
        assertEquals(true, profileResponse.public)
        repository.deleteUser(userResponse.id)

        val exception = assertFailsWith<CustomException> { userActivityService.getProfileById(userResponse.id) }
        assertEquals(Errors.UserNotFound, exception.error)
    }

    @Test
    fun getProfileByNickTest() = testApplication {
        val userResponse: UserResponse = UserRequestFactory.createUser()
        val userActivityService = UserActivityService(repository = repository)

        val profileResponseList: List<ProfileResponse> = userActivityService.getProfilesByNick(userResponse.nick)
        assertEquals(userResponse.id, profileResponseList[0].id)
        assertEquals(userResponse.nick, profileResponseList[0].nick)
        assertEquals(userResponse.bio, profileResponseList[0].bio)
        assertEquals(userResponse.type, profileResponseList[0].type)
        assertEquals(userResponse.presences, profileResponseList[0].presences)
        assertEquals(true, profileResponseList[0].public)

        val profileResponseListCaseInsensitive: List<ProfileResponse> = userActivityService.getProfilesByNick(
            userResponse.nick.uppercase(Locale.getDefault())
        )
        assertEquals(userResponse.id, profileResponseListCaseInsensitive[0].id)
        assertEquals(userResponse.nick, profileResponseListCaseInsensitive[0].nick)
        assertEquals(userResponse.bio, profileResponseListCaseInsensitive[0].bio)
        assertEquals(userResponse.type, profileResponseListCaseInsensitive[0].type)
        assertEquals(userResponse.presences, profileResponseListCaseInsensitive[0].presences)
        assertEquals(true, profileResponseListCaseInsensitive[0].public)

        val profileResponseListLowerCase: List<ProfileResponse> = userActivityService.getProfilesByNick(userResponse.nick.lowercase(
            Locale.getDefault()
        ))
        assertEquals(userResponse.id, profileResponseListLowerCase[0].id)
        assertEquals(userResponse.nick, profileResponseListLowerCase[0].nick)
        assertEquals(userResponse.bio, profileResponseListLowerCase[0].bio)
        assertEquals(userResponse.type, profileResponseListLowerCase[0].type)
        assertEquals(userResponse.presences, profileResponseListLowerCase[0].presences)
        assertEquals(true, profileResponseListLowerCase[0].public)
    }

    @Test
    fun `get presences from the user when is a public account`() = testApplication {
        val userResponse: UserResponse = UserRequestFactory.createUser()
        val userActivityService = UserActivityService(repository = repository)

        val minimalEventResponseList = userActivityService.getPresencesFromProfile(userResponse.id, userResponse.id, 1, 10)
        assertEquals(0, minimalEventResponseList.content.size)
    }

    @Test
    fun `get events from the user when is a public account`() = testApplication {
        val userResponse: UserResponse = UserRequestFactory.createUser()
        val userActivityService = UserActivityService(repository = repository)

        val minimalEventResponseList = userActivityService.getEventsFromProfile(userResponse.id, userResponse.id, 1, 10)
        assertEquals(0, minimalEventResponseList.content.size)
    }

}