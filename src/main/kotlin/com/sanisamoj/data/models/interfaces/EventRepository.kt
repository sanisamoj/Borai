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

    suspend fun getPresenceByEventAndUser(eventId: String, userId: String): Presence?
    suspend fun getPresenceByUser(userId: String, pageSize: Int = 10, pageNumber: Int = 1): List<Presence>
    suspend fun markPresencePresence(presence: Presence): Presence
    suspend fun unmarkPresencePresence(userId: String, eventId: String)
    suspend fun getPresenceById(presenceId: String): Presence
}