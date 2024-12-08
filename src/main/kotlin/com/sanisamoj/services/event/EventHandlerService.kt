package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.EventStatus
import com.sanisamoj.data.models.enums.InsigniaCriteriaType
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.data.models.interfaces.InsigniaRepository
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.utils.pagination.PaginationResponse
import com.sanisamoj.utils.pagination.paginationMethod

class EventHandlerService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository(),
    private val insigniaObserver: InsigniaRepository = GlobalContext.getInsigniaObserver()
) {

    suspend fun markPresence(userId: String, eventId: String) {
        val presenceAlreadyExist: Presence? = eventRepository.getPresenceByEventAndUser(eventId, userId)
        if(presenceAlreadyExist != null) throw CustomException(Errors.PresenceAlreadyMarked)

        val user: User = repository.getUserById(userId)
        val presence = Presence(
            eventId = eventId,
            userId = userId,
            nick = user.nick,
            imageProfile = user.imageProfile,
            accountIsPublic = user.public,
            accountType = user.type
        )

        eventRepository.markPresence(presence)
        insigniaObserver.addPoints(userId, InsigniaCriteriaType.PresencesFromTheUser, 1.0)

        val event: Event = eventRepository.getEventById(eventId)
        insigniaObserver.addPoints(event.accountId, InsigniaCriteriaType.PresencesEvents, 1.0)

        val preferences: UserPreference = user.preferences
        val creators = preferences.creators.toMutableList()

        if (!creators.contains(event.accountId)) {
            creators.add(event.accountId)
            repository.updateUser(userId, OperationField(Fields.Preferences, preferences.copy(creators = creators)))
        }
    }

    suspend fun unmarkPresence(userId: String, eventId: String) {
        eventRepository.getPresenceByEventAndUser(eventId, userId)  ?: throw CustomException(Errors.UnableToComplete)
        eventRepository.unmarkPresence(userId, eventId)
        insigniaObserver.removePoints(userId, InsigniaCriteriaType.PresencesFromTheUser, 1.0)

        val event: Event = eventRepository.getEventById(eventId)
        insigniaObserver.removePoints(event.accountId, InsigniaCriteriaType.PresencesEvents, 1.0)
    }

    suspend fun getPublicPresencesFromTheEvent(eventId: String, pageNumber: Int = 1, pageSize: Int = 10): GenericResponseWithPagination<MinimalUserResponse> {
        val presences: List<Presence> = eventRepository.getPresencesFromTheEvent(eventId, pageSize, pageNumber)
        val eventPresenceResponseList: List<MinimalUserResponse> = presences.map { it -> presenceResponseFactory(it) }

        val allPublicPresenceCount: Int = eventRepository.getPublicPresencesFromTheEventCount(eventId)
        val paginationResponse: PaginationResponse = paginationMethod(allPublicPresenceCount.toDouble(), pageSize, pageNumber)

        return GenericResponseWithPagination(content = eventPresenceResponseList, paginationResponse = paginationResponse)
    }

    suspend fun getAllPresencesFromTheEvent(eventId: String, userId: String, pageNumber: Int = 1, pageSize: Int = 10): GenericResponseWithPagination<MinimalUserResponse> {
        val event: Event = eventRepository.getEventById(eventId)
        if(event.accountId != userId) throw CustomException(Errors.TheEventHasAnotherOwner)

        val presences: List<Presence> = eventRepository.getPresencesFromTheEvent(eventId, pageSize, pageNumber)
        val eventPresenceResponseList: List<MinimalUserResponse> = presences.map { it -> presenceResponseFactory(it) }

        val allPublicPresenceCount: Int = eventRepository.getPublicPresencesFromTheEventCount(eventId)
        val paginationResponse: PaginationResponse = paginationMethod(allPublicPresenceCount.toDouble(), pageSize, pageNumber)

        return GenericResponseWithPagination(content = eventPresenceResponseList, paginationResponse = paginationResponse)
    }

    suspend fun getMutualFollowersPresences(eventId: String, userId: String): List<MinimalUserResponse> {
        val allPresences: List<Presence> = eventRepository.getAllPresencesFromTheEvent(eventId)
        val allPresencesInString: List<String> = allPresences.map { it.userId }
        val mutualFollowers: List<String> = repository.getMutualFollowers(userId)

        val minimalUserResponseList: MutableList<MinimalUserResponse> = mutableListOf()

        mutualFollowers.forEach { followerId ->
            if(allPresencesInString.contains(followerId)) {
                val presence: Presence = allPresences.find { it.userId == followerId }!!
                minimalUserResponseList.add(presenceResponseFactory(presence))
            }
        }

        return minimalUserResponseList
    }

    suspend fun addComment(userId: String, commentRequest: CommentRequest): CommentResponse {
        val user: User = repository.getUserById(userId)

        // Checks if the comment is at the first level
        commentRequest.parentId?.let {
            val comment: Comment = eventRepository.getCommentById(it)
            if(comment.parentId != null) throw CustomException(Errors.CommentsCannotExceedLevelOneResponses)
            insigniaObserver.addPoints(comment.userId, InsigniaCriteriaType.AnswerComments, 1.0)
        }

        val comment = Comment(
            eventId = commentRequest.eventId,
            userId = userId,
            nick = user.nick,
            imageProfile = user.imageProfile,
            text = commentRequest.comment,
            parentId = commentRequest.parentId
        )

        eventRepository.addComment(comment)
        insigniaObserver.addPoints(userId, InsigniaCriteriaType.Comments, 1.0)

        val event: Event = eventRepository.getEventById(commentRequest.eventId)
        insigniaObserver.addPoints(event.accountId, InsigniaCriteriaType.CommentsEvents, 1.0)

        return commentResponseFactory(comment)
    }

    suspend fun deleteComment(commentId: String, userId: String) {
        val comment: Comment = eventRepository.getCommentById(commentId)
        if(comment.userId != userId) throw CustomException(Errors.UnableToComplete)
        eventRepository.deleteComment(commentId)
        insigniaObserver.removePoints(userId, InsigniaCriteriaType.Comments, 1.0)
        comment.parentId?.let { insigniaObserver.removePoints(comment.userId, InsigniaCriteriaType.AnswerComments, 1.0) }

        val event: Event = eventRepository.getEventById(comment.eventId)
        insigniaObserver.addPoints(event.accountId, InsigniaCriteriaType.CommentsEvents, 1.0)
    }

    suspend fun getCommentsFromTheEvent(eventId: String, pageSize: Int, pageNumber: Int): GenericResponseWithPagination<CommentResponse> {
        val comments: List<Comment> = eventRepository.getCommentsFromTheEvent(eventId, pageSize, pageNumber)

        val commentsCount: Int = eventRepository.getCommentsFromTheEventCount(eventId)
        val paginationResponse: PaginationResponse = paginationMethod(commentsCount.toDouble(), pageSize, pageNumber)

        return GenericResponseWithPagination<CommentResponse>(
            content = comments.map { commentResponseFactory(it) },
            paginationResponse = paginationResponse
        )
    }

    suspend fun getParentComments(eventId: String, parentId: String, pageSize: Int, pageNumber: Int): GenericResponseWithPagination<CommentResponse> {
        val comments: List<Comment> = eventRepository.getParentComments(eventId, parentId, pageSize, pageNumber)

        val commentsCount: Int = eventRepository.getParentCommentsCount(eventId, parentId)
        val paginationResponse: PaginationResponse = paginationMethod(commentsCount.toDouble(), pageSize, pageNumber)

        return GenericResponseWithPagination<CommentResponse>(
            content = comments.map { commentResponseFactory(it) },
            paginationResponse = paginationResponse
        )
    }

    suspend fun getAllEventsFromAccount(accountId: String): List<EventResponse> {
        val events: List<Event> = eventRepository.getAllEventFromAccount(accountId)
        val eventService = EventService(eventRepository, repository)
        return events.map { eventService.eventResponseFactory(it) }
    }

    suspend fun getEventsFromAccount(accountId: String, pageSize: Int, pageNumber: Int): GenericResponseWithPagination<EventResponse> {
        val events: List<Event> = eventRepository.getAllEventFromAccountWithPagination(accountId, pageNumber, pageSize)
        val eventService = EventService(eventRepository, repository)
        val eventResponseList: List<EventResponse> = events.map { eventService.eventResponseFactory(it) }

        val eventsCount: Int = eventRepository.getAllEventFromAccountCount(accountId)
        val paginationResponse: PaginationResponse = paginationMethod(eventsCount.toDouble(), pageSize, pageNumber)

        return GenericResponseWithPagination(eventResponseList, paginationResponse)
    }

    private fun presenceResponseFactory(presence: Presence): MinimalUserResponse {
        return MinimalUserResponse(
            id = presence.userId,
            nick = presence.nick,
            imageProfile = presence.imageProfile,
            accountType = presence.accountType
        )
    }

    private fun commentResponseFactory(comment: Comment): CommentResponse {
        return CommentResponse(
            id = comment.id.toString(),
            eventId = comment.eventId,
            userId = comment.userId,
            nick = comment.nick,
            imageProfile = comment.imageProfile,
            comment = comment.text,
            parentId = comment.parentId,
            answersCount = comment.answersCount,
            createdAt = comment.createdAt.toString()
        )
    }

    suspend fun submitEventVote(userId: String, eventVote: EventVote) {
        val event: Event = eventRepository.getEventById(eventVote.eventId)
        if(event.status != EventStatus.COMPLETED.name) throw CustomException(Errors.EventNotEnded)

        val userAlreadyVote: EventVote? = event.eventVotes.find { it.userId == userId }
        if(userAlreadyVote != null) throw CustomException(Errors.UserAlreadyVoted)

        if(eventVote.rating > 5 || eventVote.rating < 1) throw CustomException(Errors.InvalidRating)

        eventRepository.getPresenceByEventAndUser(eventVote.eventId, userId)
            ?: throw CustomException(Errors.UserDidNotAttendEvent)

        eventRepository.submitEventVote(eventVote.copy(userId = userId))

        val updatedEvent: Event = eventRepository.getEventById(eventVote.eventId)
        val newScore: Double = updatedEvent.getAverageScore()
        eventRepository.updateEvent(eventVote.eventId, OperationField(Fields.Score, newScore))
    }

    suspend fun upComment(commentId: String, userId: String) {
        val comment: Comment = eventRepository.getCommentById(commentId)
        val userIdAlreadyUpVoted: String? = comment.ups.find { it == userId }
        if(userIdAlreadyUpVoted != null) throw CustomException(Errors.UserHasAlreadyUpvoted)

        eventRepository.upComment(commentId, userId)
        insigniaObserver.addPoints(comment.userId, InsigniaCriteriaType.UpsComments, 1.0)
    }

    suspend fun downComment(commentId: String, userId: String) {
        val comment: Comment = eventRepository.getCommentById(commentId)
        val userIdAlreadyUpVoted: String? = comment.ups.find { it == userId }
        if(userIdAlreadyUpVoted == null) throw CustomException(Errors.CannotRemoveUpIfNotMade)

        eventRepository.downComment(commentId, userId)
        insigniaObserver.removePoints(comment.userId, InsigniaCriteriaType.UpsComments, 1.0)
    }

}