package com.sanisamoj.routing

import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.services.event.EventHandlerService
import com.sanisamoj.services.event.EventService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime

fun Route.eventRouting() {

    route("/event") {

        authenticate("user-jwt") {

            // Responsible for creating event
            post {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val createEventRequest: CreateEventRequest = call.receive<CreateEventRequest>()
                val eventResponse: EventResponse = EventService().createEvent(accountId, createEventRequest)
                return@post call.respond(HttpStatusCode.Created, eventResponse)
            }

            // Responsible for returning events by nearby filters
            get("/nearby") {
                val longitude = call.request.queryParameters["longitude"]?.toDoubleOrNull()
                val latitude = call.request.queryParameters["latitude"]?.toDoubleOrNull()
                val maxDistanceMeters = call.request.queryParameters["maxDistanceMeters"]?.toIntOrNull()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

                if (longitude == null || latitude == null || maxDistanceMeters == null) {
                    throw CustomException(Errors.InvalidParameters)
                }

                val filters = SearchEventNearby(
                    longitude = longitude,
                    latitude = latitude,
                    maxDistanceMeters = maxDistanceMeters,
                    page = page,
                    size = size
                )

                val eventList: GenericResponseWithPagination<EventResponse> = EventService().findEventsNearby(filters)

                return@get call.respond(HttpStatusCode.OK, eventList)
            }

            // Responsible for mark presence
            post("/presence") {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val eventId: String = call.request.queryParameters["eventId"].toString()
                EventHandlerService().markPresence(accountId, eventId)
                return@post call.respond(HttpStatusCode.OK)
            }

            // Responsible for mark presence
            delete("/presence") {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val eventId: String = call.request.queryParameters["eventId"].toString()
                EventHandlerService().unmaskPresence(accountId, eventId)
                return@delete call.respond(HttpStatusCode.OK)
            }
        }

        // Responsible for returning events by filters
        get {
            val id = call.request.queryParameters["id"]

            if(id != null) {
                val eventResponse: EventResponse = EventService().getEventById(id)
                return@get call.respond(eventResponse)
            }

            val name = call.request.queryParameters["name"]
            val street = call.request.queryParameters["street"]
            val neighborhood = call.request.queryParameters["neighborhood"]
            val city = call.request.queryParameters["city"]
            val uf = call.request.queryParameters["uf"]
            val type = call.request.queryParameters["type"]?.split(",") // Esperando tipos separados por vírgula
            val status = call.request.queryParameters["status"]
            val date = call.request.queryParameters["date"]?.let {
                try {
                    LocalDateTime.parse(it)
                } catch (_: Exception) {
                    null
                }
            }
            val endDate = call.request.queryParameters["endDate"]?.let {
                try {
                    LocalDateTime.parse(it)
                } catch (_: Exception) {
                    null
                }
            }

            val filters = SearchEventFilters(
                name = name.takeIf { it?.isNotBlank() == true },
                address = if (street.isNullOrEmpty() && neighborhood.isNullOrEmpty() && city.isNullOrEmpty() && uf.isNullOrEmpty()) {
                    null
                } else {
                    AddressToSearch(
                        street = street.orEmpty().takeIf { it.isNotBlank() },
                        neighborhood = neighborhood.orEmpty().takeIf { it.isNotBlank() },
                        city = city.orEmpty().takeIf { it.isNotBlank() },
                        uf = uf.orEmpty().takeIf { it.isNotBlank() }
                    )
                },
                type = type?.takeIf { it.isNotEmpty() },
                status = status.takeIf { it?.isNotBlank() == true },
                date = date,
                endDate = endDate,
                page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1,
                size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25
            )

            val eventResponseList = EventService().searchEvents(filters)

            return@get call.respond(HttpStatusCode.OK, eventResponseList)
        }

    }

    route("/presence") {

        // Responsible for returning all public presences from the event
        get {
            val id = call.request.queryParameters["eventId"].toString()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

            val allPublicPresenceFromTheEvent: GenericResponseWithPagination<MinimalUserResponse> = EventHandlerService().getPublicPresencesFromTheEvent(
                eventId = id,
                pageNumber = page,
                pageSize = size
            )

            return@get call.respond(allPublicPresenceFromTheEvent)

        }

        authenticate("user-jwt") {

            // Responsible for returning mutual followers presence
            get("/mutual") {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val id = call.request.queryParameters["eventId"].toString()

                val minimalUserResponseList: List<MinimalUserResponse> = EventHandlerService().getMutualFollowersPresences(id, accountId)
                return@get call.respond(minimalUserResponseList)
            }

        }

    }

}
