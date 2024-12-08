package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.AccountType
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.EventStatus
import com.sanisamoj.data.models.enums.PresenceStatus
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.data.models.interfaces.InsigniaRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.services.followers.FollowerService
import com.sanisamoj.services.user.UserFactoryTest
import com.sanisamoj.services.user.UserFactoryTest.activateUser
import com.sanisamoj.utils.eraseAllDataInMongodb
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class EventHandlerServiceTest {
    private val eventRepository: EventRepository = GlobalContextTest.getEventRepository()
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()
    private val insigniaObserver: InsigniaRepository = GlobalContextTest.getInsigniaObserver()

    @AfterTest
    fun eraseAllData() {
        runBlocking {
            eraseAllDataInMongodb<User>(CollectionsInDb.Users)
            eraseAllDataInMongodb<Event>(CollectionsInDb.Events)
            eraseAllDataInMongodb<Presence>(CollectionsInDb.Presences)
            eraseAllDataInMongodb<Followers>(CollectionsInDb.Followers)
            eraseAllDataInMongodb<Followers>(CollectionsInDb.InsigniaPoints)
        }
    }

    private suspend fun createEvent(userId: String): EventResponse {
        val createEventRequest: CreateEventRequest = EventFactoryTest.generateRandomEventRequest()
        val eventResponse: EventResponse = EventService(eventRepository, repository).createEvent(userId, createEventRequest)
        return eventResponse
    }

    @Test
    fun `mark presence test`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        eventHandlerService.markPresence(user.id, eventResponse.id)

        val presences: List<Presence> = eventRepository.getAllPresencesFromTheEvent(eventResponse.id)
        val userPresence: Presence? = presences.find { it.userId == user.id }
        assertTrue(userPresence is Presence)
        assertEquals(user.nick, userPresence.nick)
        assertEquals(PresenceStatus.MARKED_PRESENT.name, userPresence.status)
        assertEquals(AccountType.PARTICIPANT.name, userPresence.accountType)
        assertTrue(userPresence.accountIsPublic)
    }

    @Test
    fun `unmark presence test`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        eventHandlerService.markPresence(user.id, eventResponse.id)

        var presences: List<Presence> = eventRepository.getAllPresencesFromTheEvent(eventResponse.id)
        var userPresence: Presence? = presences.find { it.userId == user.id }
        assertTrue(userPresence is Presence)

        eventHandlerService.unmarkPresence(user.id, eventResponse.id)
        presences = eventRepository.getAllPresencesFromTheEvent(eventResponse.id)
        userPresence = presences.find { it.userId == user.id }
        assertTrue(userPresence == null)
    }

    @Test
    fun `submit event vote test`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        eventHandlerService.markPresence(user.id, eventResponse.id)

        // Complete the event to make you eligible for voting
        eventRepository.updateEvent(eventResponse.id, OperationField(Fields.Status, EventStatus.COMPLETED.name))

        val eventVote = EventVote(eventId = eventResponse.id, rating = 4)
        eventHandlerService.submitEventVote(user.id, eventVote)

        val updatedEvent: Event = eventRepository.getEventById(eventResponse.id)
        val submittedVote = updatedEvent.eventVotes.find { it.userId == user.id }

        assertTrue(submittedVote != null)
        assertEquals(eventVote.rating, submittedVote.rating)
        assertEquals(updatedEvent.getAverageScore(), 4.0)
    }

    @Test
    fun `submit event vote should fail when event is not completed`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val eventVote = EventVote(eventId = eventResponse.id, rating = 4)

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        eventHandlerService.markPresence(user.id, eventResponse.id)

        val exception = assertFailsWith<CustomException> {
            eventHandlerService.submitEventVote(user.id, eventVote)
        }

        assertEquals(Errors.EventNotEnded, exception.error)
    }

    @Test
    fun `submit event vote should fail when user already voted`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        eventHandlerService.markPresence(user.id, eventResponse.id)

        eventRepository.updateEvent(eventResponse.id, OperationField(Fields.Status, EventStatus.COMPLETED.name))

        val eventVote = EventVote(eventId = eventResponse.id, rating = 4)
        eventHandlerService.submitEventVote(user.id, eventVote)

        val secondVote = EventVote(eventId = eventResponse.id, rating = 5)

        val exception = assertFailsWith<CustomException> {
            eventHandlerService.submitEventVote(user.id, secondVote)
        }

        assertEquals(Errors.UserAlreadyVoted, exception.error)
    }

    @Test
    fun `submit event vote should fail with invalid rating`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        eventHandlerService.markPresence(user.id, eventResponse.id)

        eventRepository.updateEvent(eventResponse.id, OperationField(Fields.Status, EventStatus.COMPLETED.name))

        val invalidVote = EventVote(eventId = eventResponse.id, rating = 6)

        val exception = assertFailsWith<CustomException> {
            eventHandlerService.submitEventVote(user.id, invalidVote)
        }

        assertEquals(Errors.InvalidRating, exception.error)
    }

    @Test
    fun `submit event vote should fail when user did not attend the event`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)

        eventRepository.updateEvent(eventResponse.id, OperationField(Fields.Status, EventStatus.COMPLETED.name))

        val eventVote = EventVote(eventId = eventResponse.id, rating = 4)

        val exception = assertFailsWith<CustomException> {
            eventHandlerService.submitEventVote(user.id, eventVote)
        }

        assertEquals(Errors.UserDidNotAttendEvent, exception.error)
    }

    @Test
    fun `up comment test`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val commentRequest = CommentRequest(eventId = eventResponse.id, comment = "This is a test comment")
        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        val commentResponse: CommentResponse = eventHandlerService.addComment(user.id, commentRequest)

        eventHandlerService.upComment(commentResponse.id, user.id)

        val updatedComment: Comment = eventRepository.getCommentById(commentResponse.id)
        assertTrue(updatedComment.ups.contains(user.id))

        val exception = assertFailsWith<CustomException> {
            eventHandlerService.upComment(commentResponse.id, user.id)
        }

        assertEquals(Errors.UserHasAlreadyUpvoted, exception.error)
    }

    @Test
    fun `down comment test`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val commentRequest = CommentRequest(eventId = eventResponse.id, comment = "This is a test comment")
        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        val commentResponse = eventHandlerService.addComment(user.id, commentRequest)

        eventHandlerService.upComment(commentResponse.id, user.id)

        eventHandlerService.downComment(commentResponse.id, user.id)

        val updatedComment: Comment = eventRepository.getCommentById(commentResponse.id)
        assertFalse(updatedComment.ups.contains(user.id))

        val exception = assertFailsWith<CustomException> {
            eventHandlerService.downComment(commentResponse.id, user.id)
        }

        assertEquals(Errors.CannotRemoveUpIfNotMade, exception.error)
    }

    @Test
    fun `delete comment test`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val commentRequest = CommentRequest(eventId = eventResponse.id, comment = "This is a test comment")
        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        val commentResponse: CommentResponse = eventHandlerService.addComment(user.id, commentRequest)

        eventHandlerService.deleteComment(commentResponse.id, user.id)

        val deletedComment: Comment? = runCatching { eventRepository.getCommentById(commentResponse.id) }.getOrNull()
        assertTrue(deletedComment == null)
    }

    @Test
    fun `get comments from the event test`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = createEvent(user.id)

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        repeat(3) {
            val commentRequest = CommentRequest(eventId = eventResponse.id, comment = "Comment $it")
            eventHandlerService.addComment(user.id, commentRequest)
        }

        val commentsResponse = eventHandlerService.getCommentsFromTheEvent(eventResponse.id, 10, 1)

        assertEquals(3, commentsResponse.content.size)
        assertTrue(commentsResponse.content.all { it.eventId == eventResponse.id })
    }

    @Test
    fun `addComment should fail when exceeding maximum response level`() = testApplication {
        val user: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = EventFactoryTest.createEvent(user.id)

        val firstComment = eventRepository.addComment(
            Comment(
                eventId = eventResponse.id,
                userId = user.id,
                nick = user.nick,
                imageProfile = user.imageProfile,
                text = "First-level comment",
                parentId = null
            )
        )

        val secondComment = eventRepository.addComment(
            Comment(
                eventId = eventResponse.id,
                userId = user.id,
                nick = user.nick,
                imageProfile = user.imageProfile,
                text = "Second-level comment",
                parentId = firstComment.id.toString()
            )
        )

        val commentRequest = CommentRequest(
            eventId = eventResponse.id,
            comment = "Third-level comment",
            parentId = secondComment.id.toString()
        )

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)

        val exception = assertFailsWith<CustomException> {
            eventHandlerService.addComment(user.id, commentRequest)
        }

        assertEquals(Errors.CommentsCannotExceedLevelOneResponses, exception.error)
    }

    @Test
    fun `deleteComment should fail when comment does not belong to the user`() = testApplication {
        val user1: UserResponse = UserFactoryTest.createUser()
        val user2: UserResponse = UserFactoryTest.createUser()
        val eventResponse: EventResponse = EventFactoryTest.createEvent(user1.id)

        val comment = eventRepository.addComment(
            Comment(
                eventId = eventResponse.id,
                userId = user1.id,
                nick = user1.nick,
                imageProfile = user1.imageProfile,
                text = "First-level comment",
                parentId = null
            )
        )

        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)

        val exception = assertFailsWith<CustomException> {
            eventHandlerService.deleteComment(comment.id.toString(), user2.id)
        }

        assertEquals(Errors.UnableToComplete, exception.error)
    }

    @Test
    fun `getMutualFollowersPresences should return mutual followers who are present in the event`() = testApplication {
        val user1: UserResponse = UserFactoryTest.createUser()
        activateUser(user1.id)

        val user2: UserResponse = UserFactoryTest.createUser()
        activateUser(user2.id)

        val user3: UserResponse = UserFactoryTest.createUser()
        activateUser(user3.id)

        val user4: UserResponse = UserFactoryTest.createUser()
        activateUser(user4.id)

        val event = EventFactoryTest.createEvent(user4.id)

        // Setup followers and mutual followers
        val followerService = FollowerService(repository)
        followerService.sendFollowRequest(user1.id, user2.id)
        followerService.acceptFollowRequest(user1.id, user2.id)
        followerService.sendFollowRequest(user2.id, user1.id)
        followerService.acceptFollowRequest(user2.id, user1.id)
        followerService.sendFollowRequest(user1.id, user3.id)
        followerService.acceptFollowRequest(user1.id, user3.id)
        followerService.sendFollowRequest(user3.id, user1.id)
        followerService.acceptFollowRequest(user3.id, user1.id)

        // Mark presences for mutual followers
        val eventHandlerService = EventHandlerService(eventRepository, repository, insigniaObserver)
        eventHandlerService.markPresence(user2.id, event.id)
        eventHandlerService.markPresence(user3.id, event.id)

        // User4 is not a mutual follower
        eventHandlerService.markPresence(user4.id, event.id)

        // Test getMutualFollowersPresences
        val result = eventHandlerService.getMutualFollowersPresences(event.id, user1.id)

        assertEquals(2, result.size)
        assertTrue(result.any { it.id == user2.id })
        assertTrue(result.any { it.id == user3.id })
    }

}