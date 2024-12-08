package com.sanisamoj.services.moderator

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.EventResponse
import com.sanisamoj.data.models.dataclass.Presence
import com.sanisamoj.data.models.dataclass.UserResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.data.models.interfaces.InsigniaRepository
import com.sanisamoj.services.event.EventFactoryTest.createEvent
import com.sanisamoj.services.event.EventHandlerService
import com.sanisamoj.services.user.UserFactoryTest
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModeratorActivityServiceTest {
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()
    private val eventRepository: EventRepository = GlobalContextTest.getEventRepository()
    private val insigniaObserver: InsigniaRepository = GlobalContextTest.getInsigniaObserver()

    @Test
    fun `getUsersWithPagination should return users with correct pagination`() = testApplication {
        val moderatorActivityService = ModeratorActivityService(repository, eventRepository)

        repeat(10) { UserFactoryTest.createUser() }

        val page = 1
        val size = 5

        val response = moderatorActivityService.getUsersWithPagination(page, size)

        assertEquals(size, response.content.size)
        assertEquals(1, response.paginationResponse.remainingPage)
        assertEquals(size, response.content.size)
        assertEquals(2, response.paginationResponse.totalPages)
    }

    @Test
    fun `deleteEvent should remove an event and its presences`() = testApplication {
        val moderatorActivityService = ModeratorActivityService(repository, eventRepository)

        val user: UserResponse = UserFactoryTest.createUser()
        val event: EventResponse = createEvent(user.id)
        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        eventHandlerService.markPresence(user.id, event.id)

        var presences: List<Presence> = eventRepository.getAllPresencesFromTheEvent(event.id)
        assertEquals(1, presences.size)

        moderatorActivityService.deleteEvent(event.id)

        presences = eventRepository.getAllPresencesFromTheEvent(event.id)

        val exception = assertFailsWith<CustomException> { eventRepository.getEventById(event.id) }

        assertTrue(presences.isEmpty())
        assertEquals(Errors.EventNotFound, exception.error)
    }


}