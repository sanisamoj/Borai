package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.EventStatus
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.services.user.UserFactoryTest
import com.sanisamoj.utils.eraseAllDataInMongodb
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.test.*

class EventServiceTest {
    private val eventRepository: EventRepository = GlobalContextTest.getEventRepository()
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()

    @AfterTest
    fun eraseAllUserData() {
        runBlocking {
            eraseAllDataInMongodb<User>(CollectionsInDb.Users)
            eraseAllDataInMongodb<User>(CollectionsInDb.Events)
        }
    }

    @Test
    fun createEventTest() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventService = EventService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = eventService.createEvent(user.id, createEventRequest)
        assertEquals(createEventRequest.name, eventResponse.name)
        assertEquals(createEventRequest.description, eventResponse.description)
        assertEquals(createEventRequest.image, eventResponse.image)
        assertEquals(createEventRequest.otherImages, eventResponse.otherImages)
        assertEquals(createEventRequest.address, eventResponse.address)
        assertEquals(createEventRequest.type, eventResponse.type)
        assertEquals(EventStatus.SCHEDULED.name, eventResponse.status)

        val requestDate: LocalDateTime = LocalDateTime.parse(createEventRequest.date, DateTimeFormatter.ISO_DATE_TIME)
        val responseDate: LocalDateTime = LocalDateTime.parse(eventResponse.date, DateTimeFormatter.ISO_DATE_TIME)
        assertEquals(requestDate, responseDate)
    }

    @Test
    fun `create event test with invalid parameters`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventService = EventService(eventRepository, repository)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()

        val exception: CustomException = assertFailsWith {
            eventService.createEvent(user.id, createEventRequest.copy(type = listOf("Wrong")))
        }

        assertEquals(Errors.InvalidParameters, exception.error)
    }

    @Test
    fun `create event with past date fails`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventService = EventService(eventRepository, repository)

        val pastDate: String = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ISO_DATE_TIME)
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest().copy(date = pastDate)

        val exception: CustomException = assertFailsWith {
            eventService.createEvent(user.id, createEventRequest)
        }

        assertEquals(Errors.TheEventDateCannotBeInThePast, exception.error)
    }

    @Test
    fun `delete event test`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventResponse: EventResponse = EventFactoryTest.createEvent(user.id)
        val eventService = EventService(eventRepository, repository)
        eventService.deleteEvent(eventResponse.id, user.id)

        val exception: CustomException = assertFailsWith<CustomException> {
            eventService.getEventById(eventResponse.id)
        }

        assertEquals(Errors.EventNotFound, exception.error)
    }

    @Test
    fun `delete event should fail when user is not the owner`() = testApplication {
        val ownerUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(ownerUser.id)

        val otherUser: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(otherUser.id)

        val eventService = EventService(eventRepository, repository)

        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = eventService.createEvent(ownerUser.id, createEventRequest)

        val exception = assertFailsWith<CustomException> {
            eventService.deleteEvent(eventResponse.id, otherUser.id)
        }

        assertEquals(Errors.TheEventHasAnotherOwner, exception.error)
    }

    @Test
    fun `search events by filters`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventService = EventService(eventRepository, repository)

        val eventRequest1 = EventFactoryTest.generateRandomEventRequest().copy(name = "Tech Conference 2024", type = listOf("Technology"))
        val eventRequest2 = EventFactoryTest.generateRandomEventRequest().copy(name = "Music Festival", type = listOf("Educational"))
        eventService.createEvent(user.id, eventRequest1)
        eventService.createEvent(user.id, eventRequest2)

        val filters = SearchEventFilters(
            name = "Tech",
            type = listOf("Technology"),
            page = 1,
            size = 10
        )

        val response = eventService.searchEvents(filters)

        // Verifica os resultados
        assertEquals(1, response.content.size)
        assertEquals("Tech Conference 2024", response.content.first().name)
        assertEquals(listOf("Technology"), response.content.first().type)
    }

    // Tests only pass separately
//    @Test
//    fun `find events nearby within specified distance`() = testApplication {
//        val user: UserResponse = UserFactoryTest.createUser()
//        UserFactoryTest.activateUser(user.id)
//
//        val eventService = EventService(eventRepository, repository)
//
//        // Set the search location
//        val searchFilters = SearchEventNearby(
//            longitude = -46.633308, // Longitude of São Paulo, for example
//            latitude = -23.550520,  // Latitude of São Paulo
//            maxDistanceMeters = 5000, // Search for events up to 5km away
//            page = 1,
//            size = 10
//        )
//
//        // Create events near and far to ensure only upcoming ones are returned
//        val nearbyEventRequest = EventFactoryTest.generateRandomEventRequest().copy(
//            name = "Nearby Event",
//            address = Address(
//                geoCoordinates = GeoCoordinates(coordinates = listOf(-46.630308, -23.555520)),
//                uf = "SP"
//            )
//        )
//
//        val distantEventRequest = EventFactoryTest.generateRandomEventRequest().copy(
//            name = "Distant Event",
//            address = Address(
//                geoCoordinates = GeoCoordinates(coordinates = listOf(-23.700520, -46.750308)),
//                uf = "SP"
//            )
//        )
//
//        eventService.createEvent(user.id, nearbyEventRequest)
//        eventService.createEvent(user.id, distantEventRequest)
//
//        val response = eventService.findEventsNearby(searchFilters)
//
//        assertEquals(1, response.content.size)
//        assertEquals("Nearby Event", response.content.first().name)
//
//        // Verifique a paginação
//        assertTrue(response.content.size == 1)
//    }
//
//    @Test
//    fun `find events nearby with filters applied`() = testApplication {
//        val user: UserResponse = UserFactoryTest.createUser()
//        UserFactoryTest.activateUser(user.id)
//
//        val eventService = EventService(eventRepository, repository)
//
//        val searchFilters = SearchEventNearby(
//            longitude = -46.633308,
//            latitude = -23.550520,
//            maxDistanceMeters = 10000,
//            type = listOf("Technology"),
//            page = 1,
//            size = 10
//        )
//
//        val techEventRequest = EventFactoryTest.generateRandomEventRequest().copy(
//            name = "Tech Event",
//            type = listOf("Technology"),
//            address = Address(
//                geoCoordinates = GeoCoordinates(coordinates = listOf(-46.630308, -23.555520)),
//                uf = "SP"
//            )
//        )
//
//        val otherEventRequest = EventFactoryTest.generateRandomEventRequest().copy(
//            name = "Sports Event",
//            type = listOf("Sports"),
//            address = Address(
//                geoCoordinates = GeoCoordinates(coordinates = listOf(-23.555520, -46.630308)),
//                uf = "SP"
//            )
//        )
//
//        eventService.createEvent(user.id, techEventRequest)
//        eventService.createEvent(user.id, otherEventRequest)
//
//        val response = eventService.findEventsNearby(searchFilters)
//
//        assertEquals(1, response.content.size)
//        assertEquals("Tech Event", response.content.first().name)
//    }
//
//    @Test
//    fun `find events nearby with no events in range`() = testApplication {
//        val user: UserResponse = UserFactoryTest.createUser()
//        UserFactoryTest.activateUser(user.id)
//
//        val eventService = EventService(eventRepository, repository)
//
//        val searchFilters = SearchEventNearby(
//            longitude = -46.633308,
//            latitude = -23.550520,
//            maxDistanceMeters = 1000,
//            page = 1,
//            size = 10
//        )
//
//        val distantEventRequest = EventFactoryTest.generateRandomEventRequest().copy(
//            name = "Distant Event",
//            address = Address(
//                geoCoordinates = GeoCoordinates(coordinates = listOf(-23.700520, -46.750308)), // Out of range
//                uf = "SP"
//            )
//        )
//        eventService.createEvent(user.id, distantEventRequest)
//
//        val response = eventService.findEventsNearby(searchFilters)
//
//        assertEquals(0, response.content.size)
//    }

    @Test
    fun `find events nearby with different conditions`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        UserFactoryTest.activateUser(user.id)

        val eventService = EventService(eventRepository, repository)

        // Set the search location
        val searchFilters = SearchEventNearby(
            longitude = -46.633308, // Longitude of São Paulo, for example
            latitude = -23.550520,  // Latitude of São Paulo
            maxDistanceMeters = 5000, // Search for events up to 5km away
            page = 1,
            size = 10
        )

        // Create events near and far to ensure only upcoming ones are returned
        val nearbyEventRequest = EventFactoryTest.generateRandomEventRequest().copy(
            name = "Nearby Event",
            address = Address(
                geoCoordinates = GeoCoordinates(coordinates = listOf(-46.630308, -23.555520)),
                uf = "SP"
            )
        )

        val distantEventRequest = EventFactoryTest.generateRandomEventRequest().copy(
            name = "Distant Event",
            address = Address(
                geoCoordinates = GeoCoordinates(coordinates = listOf(-23.700520, -46.750308)),
                uf = "SP"
            )
        )

        var event1 = eventService.createEvent(user.id, nearbyEventRequest)
        var event2 = eventService.createEvent(user.id, distantEventRequest)

        // Verify find events within range
        val response = eventService.findEventsNearby(searchFilters)
        assertEquals(1, response.content.size)
        assertEquals("Nearby Event", response.content.first().name)
        assertTrue(response.content.size == 1)
        eventRepository.deleteEvent(event1.id)
        eventRepository.deleteEvent(event2.id)

        // Test with filters applied (e.g., type)
        val searchFiltersWithType = searchFilters.copy(
            type = listOf("Technology")
        )

        val techEventRequest = EventFactoryTest.generateRandomEventRequest().copy(
            name = "Tech Event",
            type = listOf("Technology"),
            address = Address(
                geoCoordinates = GeoCoordinates(coordinates = listOf(-46.630308, -23.555520)),
                uf = "SP"
            )
        )

        val otherEventRequest = EventFactoryTest.generateRandomEventRequest().copy(
            name = "Sports Event",
            type = listOf("Sports"),
            address = Address(
                geoCoordinates = GeoCoordinates(coordinates = listOf(-23.555520, -46.630308)),
                uf = "SP"
            )
        )

        event1 = eventService.createEvent(user.id, techEventRequest)
        event2 = eventService.createEvent(user.id, otherEventRequest)

        val responseWithFilter = eventService.findEventsNearby(searchFiltersWithType)

        assertEquals(1, responseWithFilter.content.size)
        assertEquals("Tech Event", responseWithFilter.content.first().name)
        eventRepository.deleteEvent(event1.id)
        eventRepository.deleteEvent(event2.id)

        // Test with no events in range
        val searchFiltersNoEventsInRange = searchFilters.copy(
            maxDistanceMeters = 1000 // Smaller distance than available events
        )

        val distantEventRequestOutOfRange = EventFactoryTest.generateRandomEventRequest().copy(
            name = "Distant Event",
            address = Address(
                geoCoordinates = GeoCoordinates(coordinates = listOf(-90.700520, -46.750308)), // Out of range
                uf = "SP"
            )
        )
        eventService.createEvent(user.id, distantEventRequestOutOfRange)

        val responseNoEventsInRange = eventService.findEventsNearby(searchFiltersNoEventsInRange)

        assertEquals(0, responseNoEventsInRange.content.size)
    }



}