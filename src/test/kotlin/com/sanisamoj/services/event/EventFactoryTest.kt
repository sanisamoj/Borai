package com.sanisamoj.services.event

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.EventRepository
import com.sanisamoj.utils.converters.converterStringToLocalDateTime
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object EventFactoryTest {
    private val eventRepository: EventRepository = GlobalContextTest.getEventRepository()
    private val repository: DatabaseRepository = GlobalContextTest.getDatabaseRepository()

    private fun generateRandomName(): String {
        val eventNames = listOf("Tech Conference", "Music Festival", "Art Exhibition", "Startup Pitch", "Coding Bootcamp")
        return "${eventNames.random()} ${Calendar.getInstance().get(Calendar.YEAR)}"
    }

    private fun generateRandomType(): List<String> {
        val eventTypes = listOf("Technology", "Cultural", "Sports", "Religious", "Corporate")
        return List((1..3).random()) { eventTypes.random() }.distinct()
    }

    private fun generateRandomAddress(): Address {
        val cities = listOf("São Paulo", "Rio de Janeiro", "Curitiba", "Salvador", "Belo Horizonte")
        return Address(
            geoCoordinates = GeoCoordinates(coordinates = listOf(-23.55 + (Math.random() - 0.5), -46.63 + (Math.random() - 0.5))),
            zipcode = "01000-${(100..999).random()}",
            street = "Rua ${(100..999).random()}",
            houseNumber = (1..2000).random().toString(),
            complement = listOf("Suite ${(1..100).random()}", "Apt ${(1..50).random()}", null).random(),
            neighborhood = listOf("Centro", "Bela Vista", "Copacabana", "Moema", "Leblon").random(),
            city = cities.random(),
            uf = "SP"
        )
    }

    fun generateRandomEventRequest(): CreateEventRequest {
        val date: String = LocalDateTime.now()
            .plusDays((1..365).random().toLong())
            .withHour((10..22).random())
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
            .format(DateTimeFormatter.ISO_DATE_TIME)

        return CreateEventRequest(
            name = generateRandomName(),
            description = "An engaging event on ${generateRandomType().joinToString(", ")}.",
            image = "tests.jpg",
            otherImages = listOf("tests.jpg"),
            address = generateRandomAddress(),
            date = date,
            type = generateRandomType()
        )
    }

    suspend fun createEvent(userId: String): EventResponse {
        val eventRequest: CreateEventRequest = generateRandomEventRequest()
        val event = Event(
            accountId = userId,
            name = eventRequest.name,
            description = eventRequest.description,
            image = eventRequest.image,
            otherImages = eventRequest.otherImages,
            address = eventRequest.address,
            date = converterStringToLocalDateTime(eventRequest.date),
            type = eventRequest.type
        )

        eventRepository.createEvent(event)
        return eventResponseFactory(event)
    }

    private suspend fun eventResponseFactory(event: Event): EventResponse {
        val user: User = repository.getUserById(event.accountId)

        val minimalUserResponse = MinimalUserResponse(
            id = user.id.toString(),
            nick = user.nick,
            imageProfile = user.imageProfile,
            accountType = user.type
        )

        val eventResponse = EventResponse(
            id = event.id.toString(),
            eventCreator = minimalUserResponse,
            name = event.name,
            description = event.description,
            image = event.image,
            otherImages = event.otherImages,
            address = event.address,
            date = event.date.toString(),
            presences = event.presences,
            type = event.type,
            status = event.status,
            createdAt = event.createdAt.toString()
        )

        return eventResponse
    }
}