package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.utils.pagination.PaginationResponse
import com.sanisamoj.utils.pagination.paginationMethod

class EventHandlerService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository(),
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
    }

    suspend fun unmaskPresence(userId: String, eventId: String) {
        val presenceAlreadyExist: Presence? = eventRepository.getPresenceByEventAndUser(eventId, userId)
        if(presenceAlreadyExist == null) throw CustomException(Errors.UnableToComplete)
        eventRepository.unmarkPresence(userId, eventId)
    }

    suspend fun getPublicPresencesFromTheEvent(eventId: String, pageNumber: Int = 1, pageSize: Int = 10): GenericResponseWithPagination<MinimalUserResponse> {
        val presences: List<Presence> = eventRepository.getPublicPresencesFromTheEvent(eventId, pageSize, pageNumber)
        val eventPresenceResponseList: List<MinimalUserResponse> = presences.map { it -> presenceResponseFactory(it) }

        val allPublicPresenceCount: Int = eventRepository.getPublicPresencesFromTheEventCount(eventId)
        val paginationResponse: PaginationResponse = paginationMethod(allPublicPresenceCount.toDouble(), pageSize, pageNumber)

        return GenericResponseWithPagination(content = eventPresenceResponseList, paginationResponse = paginationResponse)
    }

    suspend fun getMutualFollowersPresences(eventId: String, userId: String): List<MinimalUserResponse> {
        val allPresences: List<Presence> = eventRepository.getAllPublicPresencesFromTheEvent(eventId)
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
        return commentResponseFactory(comment)
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

}