package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.Address
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.Event
import com.sanisamoj.data.models.dataclass.EventResponse
import com.sanisamoj.data.models.dataclass.SavedMediaResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.services.media.MediaService
import com.sanisamoj.utils.converters.converterStringToLocalDateTime
import io.ktor.http.content.MultiPartData

class EventManagerService(
    private val eventRepository: EventRepository = GlobalContext.getEventRepository(),
    private val repository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {

    suspend fun updateName(eventId: String, userId: String, newName: String): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Name, newName))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateDescription(eventId: String, userId: String, newDescription: String): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Description, newDescription))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateAddress(eventId: String, userId: String, newAddress: Address): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Address, newAddress))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateDate(eventId: String, userId: String, newDate: String): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Date, converterStringToLocalDateTime(newDate)))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateType(eventId: String, userId: String, newTypes: List<String>): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Type, newTypes))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updateStatus(eventId: String, userId: String, newStatus: String): EventResponse {
        verifyIfAccountIsOwner(userId, eventId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Status, newStatus))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun updatePrincipalImage(eventId: String, userId: String, multipartData: MultiPartData): EventResponse {
        val event: Event = eventRepository.getEventById(eventId)
        verifyIfAccountIsOwner(userId, eventId)

        val mediaService = MediaService()
        try {
            mediaService.deleteMedia(event.image, userId)
        } catch (_: Throwable) {}

        val savedMediaResponse: List<SavedMediaResponse> = mediaService.savePublicMedia(multipartData, userId)
        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.Image, savedMediaResponse[0].filename))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun addImageToEvent(eventId: String, userId: String, multipartData: MultiPartData): EventResponse {
        val event: Event = eventRepository.getEventById(eventId)
        verifyIfAccountIsOwner(userId, eventId)

        val mediaService = MediaService()
        val savedMediaResponse: List<SavedMediaResponse> = mediaService.savePublicMedia(multipartData, userId)

        val updatedOtherImages = event.otherImages?.toMutableList() ?: mutableListOf()
        updatedOtherImages.add(savedMediaResponse[0].filename)

        val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.OtherImages, updatedOtherImages))
        return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
    }

    suspend fun removeImageFromEvent(eventId: String, userId: String, imageFilename: String): EventResponse {
        val event: Event = eventRepository.getEventById(eventId)
        verifyIfAccountIsOwner(userId, eventId)

        if (event.otherImages?.contains(imageFilename) == true) {
            val updatedOtherImages = event.otherImages.toMutableList()
            updatedOtherImages.remove(imageFilename)

            val mediaService = MediaService()
            try {
                mediaService.deleteMedia(imageFilename, userId)
            } catch (_: Throwable) {}

            val updatedEvent: Event = eventRepository.updateEvent(eventId, OperationField(Fields.OtherImages, updatedOtherImages))
            return EventService(eventRepository, repository).eventResponseFactory(updatedEvent)
        } else {
            throw CustomException(Errors.ImageNotFoundInOtherImages)
        }
    }

    private suspend fun verifyIfAccountIsOwner(accountId: String, eventId: String) {
        val event: Event = eventRepository.getEventById(eventId)
        if(event.accountId != accountId) throw CustomException(Errors.TheEventHasAnotherOwner)
    }

}