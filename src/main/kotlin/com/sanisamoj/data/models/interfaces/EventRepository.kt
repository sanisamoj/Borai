package com.sanisamoj.data.models.interfaces

import com.sanisamoj.data.models.dataclass.Event
import com.sanisamoj.data.models.dataclass.Presence
import com.sanisamoj.data.models.dataclass.SearchEventFilters
import com.sanisamoj.data.models.dataclass.SearchEventNearby

interface EventRepository {
    suspend fun createEvent(event: Event): Event
    suspend fun getEventById(eventId: String): Event
    suspend fun getAllEventFromAccount(accountId: String): List<Event>
    suspend fun getAllEventFromAccountWithPagination(accountId: String, page: Int, size: Int): List<Event>
    suspend fun searchEvents(searchEventFilters: SearchEventFilters): List<Event>
    suspend fun findEventsNearby(filters: SearchEventNearby): List<Event>
    suspend fun incrementPresence(eventId: String)
    suspend fun decrementPresence(eventId: String)
    suspend fun getEventsWithFilterCount(searchEventFilters: SearchEventFilters): Int
    suspend fun getEventsWithFilterCount(searchEventFilters: SearchEventNearby): Int

    suspend fun getPresenceByEventAndUser(eventId: String, userId: String): Presence?
    suspend fun getPublicPresencesFromTheEvent(eventId: String, pageSize: Int = 10, pageNumber: Int = 1): List<Presence>
    suspend fun getPublicPresencesFromTheEventCount(eventId: String): Int
    suspend fun getAllPublicPresencesFromTheEvent(eventId: String): List<Presence>
    suspend fun getPresenceByUser(userId: String, pageSize: Int = 10, pageNumber: Int = 1): List<Presence>
    suspend fun getPresenceById(presenceId: String): Presence

    suspend fun markPresence(presence: Presence): Presence
    suspend fun unmarkPresence(userId: String, eventId: String)
}