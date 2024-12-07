package com.sanisamoj.services.user

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.BotRepository
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.services.media.MediaService
import com.sanisamoj.utils.eraseAllDataInMongodb
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import io.ktor.utils.io.streams.*
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.*

class UserManagerServiceTest {
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()
    private val botRepository: BotRepository = GlobalContextTest.getBotRepository()

    @AfterTest
    fun eraseAllUserData() {
        runBlocking { eraseAllDataInMongodb<User>(CollectionsInDb.Users) }
    }

    private suspend fun createUser(): UserResponse {
        val userService = UserService(repository)
        val userCreateRequest: UserCreateRequest = UserRequestFactory.validUserCreateRequest()
        val userResponse: UserResponse = userService.createUser(userCreateRequest)
        return userResponse
    }

    private fun createMultiPartDataFromFile(file: File, fieldName: String = "image"): MultiPartData {
        return object : MultiPartData {
            private var isPartRead = false // Controla se a parte já foi lida

            override suspend fun readPart(): PartData? {
                if (isPartRead) return null // Retorna null após a primeira leitura
                isPartRead = true
                return PartData.FileItem(
                    { file.inputStream().asInput() }, // Converte o arquivo para InputStream
                    dispose = { file.inputStream().close() },
                    partHeaders = Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"$fieldName\"; filename=\"${file.name}\"")
                        append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString()) // Ajuste o tipo conforme o arquivo
                    }
                )
            }
        }
    }

    @Test
    fun `update name test`() = testApplication {
        val userResponse: UserResponse = createUser()

        val userManagerService = UserManagerService(repository, botRepository)
        userManagerService.updateName(userResponse.id, "newName")

        val updatedUser: User = repository.getUserById(userResponse.id)
        assertEquals("newName", updatedUser.username)
    }

    @Test
    fun `update nick test`() = testApplication {
        val userResponse: UserResponse = createUser()

        val userManagerService = UserManagerService(repository, botRepository)
        userManagerService.updateNick(userResponse.id, "nick")

        val updatedUser: User = repository.getUserById(userResponse.id)
        assertEquals("nick", updatedUser.nick)
    }

    @Test
    fun `update image profile test`() = testApplication {
        val userResponse: UserResponse = createUser()

        val userManagerService = UserManagerService(repository, botRepository)

        val mediaService = MediaService(repository)
        val testFile: File = mediaService.getMedia("tests.jpg")
        val multipartData: MultiPartData = createMultiPartDataFromFile(testFile)

        val updatedResponse: UserResponse = userManagerService.updateImageProfile(multipartData, userResponse.id)
        assertContains(updatedResponse.imageProfile!!, "tests.jpg")

        mediaService.deleteMedia(updatedResponse.imageProfile!!, updatedResponse.id)
    }

    @Test
    fun `update bio test`() = testApplication {
        val userResponse: UserResponse = createUser()

        val userManagerService = UserManagerService(repository, botRepository)
        userManagerService.updateBio(userResponse.id, "newBio")

        val updatedUser: User = repository.getUserById(userResponse.id)
        assertEquals("newBio", updatedUser.bio)
    }

    @Test
    fun `update phone process`() = testApplication {
        val userResponse: UserResponse = createUser()
        val userManagerService = UserManagerService(repository, botRepository)
        val newPhone = "5511988502686"

        userManagerService.updatePhoneProcess(userResponse.id, newPhone)
        val updatedUser: User = repository.getUserById(userResponse.id)

        assertFails {
            userManagerService.validateValidationCodeToUpdatePhone(userResponse.id, newPhone, 100000)
        }

        val updatedUserResponse: UserResponse = userManagerService.validateValidationCodeToUpdatePhone(userResponse.id, newPhone, updatedUser.validationCode!!)
        assertEquals(newPhone, updatedUserResponse.phone)
    }

    @Test
    fun `update address test`() = testApplication {
        val userResponse: UserResponse = createUser()
        val userManagerService = UserManagerService(repository, botRepository)
        val newAddress = Address(
            zipcode = "04177070",
            street = "Rua cintra",
            houseNumber = "17",
            complement = "Logo ali",
            neighborhood = "Pinheiros",
            city = "São Paulo",
            uf = "SP"
        )

        userManagerService.updateAddress(userResponse.id, newAddress)
        val updatedUser: User = repository.getUserById(userResponse.id)
        assertEquals(newAddress, updatedUser.address)
    }

    @Test
    fun `media storage test`() = testApplication {
        val userResponse: UserResponse = createUser()
        val userManagerService = UserManagerService(repository, botRepository)

        val mediaService = MediaService(repository)
        val testFile: File = mediaService.getMedia("tests.jpg")
        val multipartData: MultiPartData = createMultiPartDataFromFile(testFile)

        val updatedResponse: UserResponse = userManagerService.updateImageProfile(multipartData, userResponse.id)
        var mediaStorage: List<MediaStorage> = userManagerService.getAllMediaStorage(userResponse.id)
        assertEquals(1, mediaStorage.size)
        assertEquals(mediaStorage[0].filename, updatedResponse.imageProfile)

        val multipartData2: MultiPartData = createMultiPartDataFromFile(testFile)
        val mediaStorageList: List<MediaStorage> = userManagerService.addMediaToTheMediaStorage(multipartData2, userResponse.id)
        mediaStorage = userManagerService.getAllMediaStorage(userResponse.id)
        assertEquals(2, mediaStorage.size)
        assertEquals(mediaStorageList[1].filename, mediaStorage[1].filename)

        mediaStorageList.forEach {
            mediaService.deleteMedia(it.filename, userResponse.id)
        }
    }

    @Test
    fun `add event preference test`() = testApplication {
        val userResponse: UserResponse = createUser()
        val userManagerService = UserManagerService(repository, botRepository)

        val newPreference = "Educational"
        val updatedResponse: UserResponse = userManagerService.addEventPreference(userResponse.id, newPreference)

        assertContains(updatedResponse.preferences!!.eventPreferences, newPreference)

        val exception = assertFailsWith<CustomException> {
            userManagerService.addEventPreference(userResponse.id, newPreference)
        }

        assertEquals(Errors.DuplicatePreference, exception.error)
    }

    @Test
    fun `remove event preference test`() = testApplication {
        val userResponse: UserResponse = createUser()
        val userManagerService = UserManagerService(repository, botRepository)

        val existingPreference = "Educational"
        userManagerService.addEventPreference(userResponse.id, existingPreference)

        val updatedResponse: UserResponse = userManagerService.removeEventPreference(userResponse.id, existingPreference)
        assertFalse(updatedResponse.preferences!!.eventPreferences.contains(existingPreference))

        val exception = assertFailsWith<CustomException> {
            userManagerService.removeEventPreference(userResponse.id, existingPreference)
        }
        assertEquals(Errors.UnableToComplete, exception.error)

    }

    @Test
    fun `invalid event preference test`() = testApplication {
        val userResponse: UserResponse = createUser()
        val userManagerService = UserManagerService(repository, botRepository)

        val invalidPreference = "InvalidPreference"

        assertFailsWith<CustomException> {
            userManagerService.addEventPreference(userResponse.id, invalidPreference)
        }

        assertFailsWith<CustomException> {
            userManagerService.removeEventPreference(userResponse.id, invalidPreference)
        }
    }

}