package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.EventStatus
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.services.media.MediaService
import com.sanisamoj.services.user.UserFactoryTest
import com.sanisamoj.utils.eraseAllDataInMongodb
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.*

class EventManagerServiceTest {
    private val eventRepository: EventRepository = GlobalContextTest.getEventRepository()
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()

    @AfterTest
    fun eraseAllData() {
        runBlocking {
            eraseAllDataInMongodb<User>(CollectionsInDb.Users)
            eraseAllDataInMongodb<Event>(CollectionsInDb.Events)
        }
    }

    private fun createMultiPartDataFromFile(file: File, fieldName: String = "image"): MultiPartData {
        return object : MultiPartData {
            private var isPartRead = false // Controla se a parte já foi lida

            override suspend fun readPart(): PartData? {
                if (isPartRead) return null // Retorna null após a primeira leitura
                isPartRead = true
                val byteArray = file.readBytes()
                val byteReadChannel = ByteReadChannel(byteArray)
                return PartData.FileItem(
                    { byteReadChannel }, // Converte o arquivo para InputStream
                    dispose = { file.inputStream().close() },
                    partHeaders = Headers.build {
                        append(HttpHeaders.ContentDisposition, "form-data; name=\"$fieldName\"; filename=\"${file.name}\"")
                        append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                    }
                )
            }
        }
    }

    @Test
    fun `update event name should succeed when user is the owner`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(user.id, createEventRequest)

        val newName = "Updated Event Name"
        val updatedEventResponse = eventManagerService.updateName(eventResponse.id, user.id, newName)

        assertEquals(newName, updatedEventResponse.name)
    }

    @Test
    fun `update event name should fail when user is not the owner`() = testApplication {
        val ownerUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(ownerUser.id)

        val otherUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(otherUser.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(ownerUser.id, createEventRequest)

        val newName = "Updated Event Name"
        val exception = assertFailsWith<CustomException> {
            eventManagerService.updateName(eventResponse.id, otherUser.id, newName)
        }

        assertEquals(Errors.TheEventHasAnotherOwner, exception.error)
    }

    @Test
    fun `update event description should succeed when user is the owner`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(user.id, createEventRequest)

        val newDescription = "Updated Event Description"
        val updatedEventResponse = eventManagerService.updateDescription(eventResponse.id, user.id, newDescription)

        assertEquals(newDescription, updatedEventResponse.description)
    }

    @Test
    fun `update event description should fail when user is not the owner`() = testApplication {
        val ownerUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(ownerUser.id)

        val otherUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(otherUser.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(ownerUser.id, createEventRequest)

        val newDescription = "Updated Event Description"
        val exception = assertFailsWith<CustomException> {
            eventManagerService.updateDescription(eventResponse.id, otherUser.id, newDescription)
        }

        assertEquals(Errors.TheEventHasAnotherOwner, exception.error)
    }

    @Test
    fun `update event address should succeed when user is the owner`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(user.id, createEventRequest)

        val newAddress = Address(
            geoCoordinates = GeoCoordinates(coordinates = listOf(-46.630308, -23.555520)),
            uf = "SP"
        )
        val updatedEventResponse = eventManagerService.updateAddress(eventResponse.id, user.id, newAddress)

        assertEquals(newAddress, updatedEventResponse.address)
    }

    @Test
    fun `update event address should fail when user is not the owner`() = testApplication {
        val ownerUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(ownerUser.id)

        val otherUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(otherUser.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(ownerUser.id, createEventRequest)

        val newAddress = Address(
            geoCoordinates = GeoCoordinates(coordinates = listOf(-46.630308, -23.555520)),
            uf = "SP"
        )
        val exception = assertFailsWith<CustomException> {
            eventManagerService.updateAddress(eventResponse.id, otherUser.id, newAddress)
        }

        assertEquals(Errors.TheEventHasAnotherOwner, exception.error)
    }

    @Test
    fun `update event type should succeed when user is the owner`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(user.id, createEventRequest)

        val newTypes = listOf("Technology", "Business")
        val updatedEventResponse = eventManagerService.updateType(eventResponse.id, user.id, newTypes)

        assertEquals(newTypes, updatedEventResponse.type)
    }

    @Test
    fun `update event status should succeed when user is the owner`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(user.id, createEventRequest)

        val newStatus = EventStatus.ONGOING.name
        val updatedEventResponse = eventManagerService.updateStatus(eventResponse.id, user.id, newStatus)

        assertEquals(newStatus, updatedEventResponse.status)
    }

    @Test
    fun `update event status should fail when user is not the owner`() = testApplication {
        val ownerUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(ownerUser.id)

        val otherUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(otherUser.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(ownerUser.id, createEventRequest)

        val newStatus = EventStatus.ONGOING.name
        val exception = assertFailsWith<CustomException> {
            eventManagerService.updateStatus(eventResponse.id, otherUser.id, newStatus)
        }

        assertEquals(Errors.TheEventHasAnotherOwner, exception.error)
    }

    @Test
    fun `add image to event should succeed when user is the owner`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(user.id, createEventRequest)

        val mediaService = MediaService(repository)
        val testFile: File = mediaService.getMedia("tests.jpg")
        val multipartData: MultiPartData = createMultiPartDataFromFile(testFile)
        val updatedEventResponse = eventManagerService.addImageToEvent(eventResponse.id, user.id, multipartData)

        val size: Int = updatedEventResponse.otherImages?.size ?: 0

        assertEquals(1, size)
    }

    @Test
    fun `remove image from event should succeed when user is the owner and image exists`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(user.id, createEventRequest.copy(otherImages = listOf("tests.jpg")))

        val mediaService = MediaService(repository)
        val testFile: File = mediaService.getMedia("tests.jpg")
        val multipartData: MultiPartData = createMultiPartDataFromFile(testFile)
        val eventResponseWithImage: EventResponse = eventManagerService.addImageToEvent(eventResponse.id, user.id, multipartData)

        val imageFilename: String = eventResponseWithImage.otherImages?.get(1)!!
        val updatedEventResponse: EventResponse = eventManagerService.removeImageFromEvent(eventResponse.id, user.id, imageFilename)
        assertFalse(updatedEventResponse.otherImages?.contains(imageFilename) == true)
    }

    @Test
    fun `remove image from event should fail when user is not the owner`() = testApplication {
        val ownerUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(ownerUser.id)

        val otherUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(otherUser.id)

        val eventManagerService = EventManagerService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(ownerUser.id, createEventRequest)

        val mediaService = MediaService(repository)
        val testFile: File = mediaService.getMedia("tests.jpg")
        val multipartData: MultiPartData = createMultiPartDataFromFile(testFile)
        val eventResponseWithImage: EventResponse = eventManagerService.addImageToEvent(eventResponse.id, ownerUser.id, multipartData)

        val imageFilename: String = eventResponseWithImage.otherImages?.get(0)!!
        val exception = assertFailsWith<CustomException> {
            eventManagerService.removeImageFromEvent(eventResponse.id, otherUser.id, imageFilename)
        }

        assertEquals(Errors.TheEventHasAnotherOwner, exception.error)
    }
}