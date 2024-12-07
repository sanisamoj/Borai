package com.sanisamoj.services

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.AccountStatus
import com.sanisamoj.data.models.enums.AccountType
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.MailRepository
import com.sanisamoj.data.models.interfaces.SessionRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.services.user.UserAuthenticationService
import com.sanisamoj.services.user.UserService
import com.sanisamoj.utils.analyzers.dotEnv
import com.sanisamoj.utils.eraseAllDataInMongodb
import com.sanisamoj.utils.generators.Token
import com.sanisamoj.utils.generators.TokenInfo
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import org.bson.types.ObjectId
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UserAuthenticationServiceTest {
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()
    private val sessionRepository: SessionRepository = GlobalContextTest.getSessionRepository()
    private val mailRepository: MailRepository = GlobalContextTest.getMailRepository()

    @AfterTest
    fun eraseAllUserData() {
        runBlocking { eraseAllDataInMongodb<User>(CollectionsInDb.Users) }
    }

    private fun validUserCreateRequest(): UserCreateRequest {
        return UserCreateRequest(
            nick = "user123",
            bio = "I love coding and testing",
            username = "user123",
            imageProfile = null,
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

    private suspend fun createUser(): UserResponse {
        val userService = UserService(repository)
        val userCreateRequest: UserCreateRequest = validUserCreateRequest()
        val userResponse: UserResponse = userService.createUser(userCreateRequest)
        return userResponse
    }

    private suspend fun activateUser(userId: String) {
        repository.updateUser(userId, OperationField(Fields.AccountStatus, AccountStatus.Active.name))
    }

    @Test
    fun `generate validation email token and confirm account`() = testApplication {
        val userResponse: UserResponse = createUser()
        val userAuthenticationService = UserAuthenticationService(repository, sessionRepository, mailRepository)

        val user: User = repository.getUserById(userResponse.id)

        val tokenInfo = TokenInfo(
            id = user.id.toString(),
            email = user.email,
            sessionId = ObjectId().toString(),
            secret = dotEnv("USER_SECRET"),
            time = GlobalContext.EMAIL_TOKEN_EXPIRATION
        )

        val token: String = Token.generate(tokenInfo)

        assertFailsWith<Exception> {
            userAuthenticationService.activateAccountByToken("dsfljksdkfj.1234932.dsjfdsfsd")
        }

        userAuthenticationService.activateAccountByToken(token)
        val updatedUser: User = repository.getUserById(userResponse.id)
        assertEquals(AccountStatus.Active.name ,updatedUser.accountStatus)
    }

    @Test
    fun loginTest() = testApplication {
        val userResponse: UserResponse = createUser()
        val userAuthenticationService = UserAuthenticationService(repository, sessionRepository, mailRepository)

        val loginRequest = LoginRequest(
            email = "user123@example.com",
            password = "securePassword123",
        )

        val inactiveAccountException = assertFailsWith<CustomException> {
            userAuthenticationService.login(loginRequest)
        }
        assertEquals(Errors.InactiveAccount, inactiveAccountException.error)

        activateUser(userResponse.id)
        val loginResponse: LoginResponse = userAuthenticationService.login(loginRequest)
        assertEquals(userResponse, loginResponse.account)

        val exception = assertFailsWith<CustomException> {
            userAuthenticationService.login(loginRequest.copy(password = "wrongPassword"))
        }
        assertEquals(Errors.InvalidLogin, exception.error)
    }

    @Test
    fun sessionTest() = testApplication {
        val userResponse: UserResponse = createUser()
        activateUser(userResponse.id)
        val userAuthenticationService = UserAuthenticationService(repository, sessionRepository, mailRepository)

        val sessionResponse: UserResponse = userAuthenticationService.session(userResponse.id)
        assertEquals(userResponse, sessionResponse)

    }
}