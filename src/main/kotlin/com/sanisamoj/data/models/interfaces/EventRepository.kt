package com.sanisamoj.data.models.interfaces

import com.sanisamoj.data.models.dataclass.Event

interface EventRepository {
    suspend fun createEvent(event: Event): Event
    suspend fun getEventById(eventId: String): Event?
    suspend fun getEventByName(eventName: String): Event?
    suspend fun getAllEventFromAccount(accountId: String): List<Event>
    suspend fun getAllEventFromAccountWithPagination(accountId: String, page: Int, size: Int): List<Event>
    suspend fun findEventsNearby(longitude: Double, latitude: Double, maxDistanceMeters: Int): List<Event>
}