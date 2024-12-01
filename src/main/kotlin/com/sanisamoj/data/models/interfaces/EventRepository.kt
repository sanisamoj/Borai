package com.sanisamoj.data.models.interfaces

import com.sanisamoj.data.models.dataclass.Comment
import com.sanisamoj.data.models.dataclass.Event
import com.sanisamoj.data.models.dataclass.EventVote
import com.sanisamoj.data.models.dataclass.Presence
import com.sanisamoj.data.models.dataclass.SearchEventFilters
import com.sanisamoj.data.models.dataclass.SearchEventNearby
import com.sanisamoj.database.mongodb.OperationField

interface EventRepository {
    suspend fun createEvent(event: Event): Event
    suspend fun deleteEvent(eventId: String)
    suspend fun getEventById(eventId: String): Event
    suspend fun getAllEventFromAccount(accountId: String): List<Event>
    suspend fun getAllEventFromAccountCount(accountId: String): Int
    suspend fun getAllEventFromAccountWithPagination(accountId: String, page: Int, size: Int): List<Event>
    suspend fun searchEvents(filters: SearchEventFilters): List<Event>
    suspend fun findEventsNearby(filters: SearchEventNearby): List<Event>
    suspend fun getEventsWithFilterCount(filters: SearchEventFilters): Int
    suspend fun getEventsWithFilterCount(filters: SearchEventNearby): Int
    suspend fun updateEvent(eventId: String, update: OperationField): Event

    suspend fun getPresenceByEventAndUser(eventId: String, userId: String): Presence?
    suspend fun getPresencesFromTheEvent(eventId: String, pageSize: Int = 10, pageNumber: Int = 1, public: Boolean = true): List<Presence>
    suspend fun getPublicPresencesFromTheEventCount(eventId: String): Int
    suspend fun getAllPresencesFromTheEventCount(eventId: String): Int
    suspend fun getAllPresencesFromTheEvent(eventId: String): List<Presence>
    suspend fun getPresenceByUser(userId: String, pageSize: Int = 10, pageNumber: Int = 1): List<Presence>
    suspend fun getAllPresenceByUser(userId: String): List<Presence>
    suspend fun getPresenceByUserCount(userId: String): Int
    suspend fun getPresenceById(presenceId: String): Presence

    suspend fun markPresence(presence: Presence): Presence
    suspend fun unmarkPresence(userId: String, eventId: String)

    suspend fun addComment(comment: Comment): Comment
    suspend fun getCommentById(commentId: String): Comment
    suspend fun getCommentsFromTheEvent(eventId: String, pageSize: Int = 10, pageNumber: Int = 1): List<Comment>
    suspend fun getParentComments(eventId: String, parentId: String, pageSize: Int = 10, pageNumber: Int = 1): List<Comment>
    suspend fun getCommentsFromTheEventCount(eventId: String): Int
    suspend fun getParentCommentsCount(eventId: String, parentId: String): Int
    suspend fun deleteComment(commentId: String)

    suspend fun submitEventVote(eventVote: EventVote)
    suspend fun upComment(commentId: String, userId: String)
    suspend fun downComment(commentId: String, userId: String)
}