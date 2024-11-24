package com.sanisamoj.services.user

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.Event
import com.sanisamoj.data.models.dataclass.GenericResponseWithPagination
import com.sanisamoj.data.models.dataclass.MinimalEventResponse
import com.sanisamoj.data.models.dataclass.Presence
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.utils.pagination.PaginationResponse
import com.sanisamoj.utils.pagination.paginationMethod

class UserActivityService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository()
) {

    suspend fun getPresenceByUser(userId: String, pageSize: Int, pageNumber: Int): GenericResponseWithPagination<MinimalEventResponse> {
        val presences: List<Presence> = eventRepository.getPresenceByUser(userId, pageSize, pageNumber)
        val presencesCount: Double = eventRepository.getPresenceByUserCount(userId).toDouble()
        val paginationResponse: PaginationResponse = paginationMethod(presencesCount, pageSize, pageNumber)

        return GenericResponseWithPagination(
            content = presences.map { minimalEventResponseFactory(it.eventId) },
            paginationResponse = paginationResponse
        )
    }

    private suspend fun minimalEventResponseFactory(eventId: String): MinimalEventResponse {
        val event: Event = eventRepository.getEventById(eventId)
        return MinimalEventResponse(
            name = event.name,
            description = event.description,
            image = event.image,
            presences = event.presences,
            type = event.type,
            date = event.date.toString()
        )
    }
}