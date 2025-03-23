package com.sanisamoj.routing

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.EventStatus
import com.sanisamoj.services.event.EventHandlerService
import com.sanisamoj.services.event.EventManagerService
import com.sanisamoj.services.event.EventService
import com.sanisamoj.utils.analyzers.isInEnum
import com.sanisamoj.utils.converters.BytesConverter
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDateTime

fun Route.eventRouting() {

    route("/events") {

        authenticate("user-jwt", "moderator-jwt") {

            // Responsible for creating event
            post {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val createEventRequest: CreateEventRequest = call.receive<CreateEventRequest>()
                val eventResponse: EventResponse = EventService().createEvent(accountId, createEventRequest)
                return@post call.respond(HttpStatusCode.Created, eventResponse)
            }

            // Responsible for update event
            put {
                val principal: JWTPrincipal = call.principal()!!
                val userId: String = principal.payload.getClaim("id").asString()
                val eventId: String = call.parameters["eventId"].toString()
                val putEventRequest: PutEventRequest = call.receive<PutEventRequest>()
                val eventManagerService = EventManagerService()
                var updatedEventResponse: EventResponse

                when {
                    putEventRequest.name != null -> {
                        updatedEventResponse = eventManagerService.updateName(eventId, userId, putEventRequest.name)
                    }
                    putEventRequest.description != null -> {
                        updatedEventResponse = eventManagerService.updateDescription(eventId, userId, putEventRequest.description)
                    }
                    putEventRequest.address != null -> {
                        updatedEventResponse = eventManagerService.updateAddress(eventId, userId, putEventRequest.address)
                    }
                    putEventRequest.date != null -> {
                        updatedEventResponse = eventManagerService.updateDate(eventId, userId, putEventRequest.date)
                    }
                    putEventRequest.type != null -> {
                        updatedEventResponse = eventManagerService.updateType(eventId, userId, putEventRequest.type)
                    }
                    putEventRequest.status != null -> {
                        if(!putEventRequest.status.isInEnum<EventStatus>()) throw CustomException(Errors.InvalidParameters)
                        updatedEventResponse = eventManagerService.updateStatus(eventId, userId, putEventRequest.status)
                    }
                    else -> {
                        return@put call.respond(HttpStatusCode.BadRequest, "No valid data to update")
                    }
                }

                return@put call.respond(updatedEventResponse)
            }

            // Responsible for deleting event
            delete {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val eventId: String = call.parameters["eventId"].toString()

                EventService().deleteEvent(eventId, accountId)
                return@delete call.respond(HttpStatusCode.OK)
            }

            // Responsible for update principal image from the event
            put("/event-img") {
                val principal: JWTPrincipal = call.principal()!!
                val userId: String = principal.payload.getClaim("id").asString()
                val eventId: String = call.parameters["eventId"].toString()

                val multipartData: MultiPartData = call.receiveMultipart()
                val requestSize: String? = call.request.headers[HttpHeaders.ContentLength]
                val requestSizeInMb: Double = BytesConverter(requestSize!!.toLong()).getInMegabyte()
                if (requestSizeInMb > GlobalContext.MAX_HEADERS_SIZE) throw CustomException(Errors.TheLimitMaxImageAllowed)

                val eventResponse: EventResponse = EventManagerService().updatePrincipalImage(eventId, userId, multipartData)
                return@put call.respond(eventResponse)
            }

            // Responsible for add image to the event
            post("/event-img") {
                val principal: JWTPrincipal = call.principal()!!
                val userId: String = principal.payload.getClaim("id").asString()
                val eventId: String = call.parameters["eventId"].toString()

                val multipartData: MultiPartData = call.receiveMultipart()
                val requestSize: String? = call.request.headers[HttpHeaders.ContentLength]
                val requestSizeInMb: Double = BytesConverter(requestSize!!.toLong()).getInMegabyte()
                if (requestSizeInMb > GlobalContext.MAX_HEADERS_SIZE) throw CustomException(Errors.TheLimitMaxImageAllowed)

                val eventResponse: EventResponse = EventManagerService().addImageToEvent(eventId, userId, multipartData)
                return@post call.respond(eventResponse)
            }

            // Responsible for delete image to the event
            delete("/event-img") {
                val principal: JWTPrincipal = call.principal()!!
                val userId: String = principal.payload.getClaim("id").asString()
                val eventId: String = call.parameters["eventId"].toString()
                val filename: String = call.parameters["filename"].toString()

                val eventResponse: EventResponse = EventManagerService().removeImageFromEvent(eventId, userId, filename)
                return@delete call.respond(eventResponse)
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
                EventHandlerService().unmarkPresence(accountId, eventId)
                return@delete call.respond(HttpStatusCode.OK)
            }

            // Responsible for submit event vote
            post("/vote") {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val eventVote: EventVote = call.receive()
                EventHandlerService().submitEventVote(accountId, eventVote)
                return@post call.respond(HttpStatusCode.OK)
            }
        }

        // Responsible for returning events by nearby filters
        get("/nearby") {
            val longitude = call.request.queryParameters["longitude"]?.toDoubleOrNull()
            val latitude = call.request.queryParameters["latitude"]?.toDoubleOrNull()
            val maxDistanceMeters = call.request.queryParameters["maxDistanceMeters"]?.toIntOrNull()
            val nick = call.request.queryParameters["nick"]
            val type = call.request.queryParameters["type"]?.split(",") // Expecting comma separated types
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25
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

            if (longitude == null || latitude == null || maxDistanceMeters == null) {
                throw CustomException(Errors.InvalidParameters)
            }

            val filters = SearchEventNearby(
                longitude = longitude,
                latitude = latitude,
                maxDistanceMeters = maxDistanceMeters,
                nick = nick.takeIf { it?.isNotBlank() == true },
                type = type?.takeIf { it.isNotEmpty() },
                date = date,
                endDate = endDate,
                page = page,
                size = size
            )

            val eventList: GenericResponseWithPagination<EventResponse> = EventService().findEventsNearby(filters)

            return@get call.respond(HttpStatusCode.OK, eventList)
        }

        // Responsible for returning events by filters
        get("/search") {
            val id = call.request.queryParameters["id"]

            if(id != null) {
                val eventResponse: EventResponse = EventService().getEventById(id)
                return@get call.respond(eventResponse)
            }

            val name = call.request.queryParameters["name"]
            val nick = call.request.queryParameters["nick"]
            val street = call.request.queryParameters["street"]
            val neighborhood = call.request.queryParameters["neighborhood"]
            val city = call.request.queryParameters["city"]
            val uf = call.request.queryParameters["uf"]
            val type = call.request.queryParameters["type"]?.split(",") // Expecting comma separated types
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
                nick = nick.takeIf { it?.isNotBlank() == true },
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

        authenticate("user-jwt", "moderator-jwt") {

            // Responsible for returning mutual followers presence
            get("/mutual") {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val id = call.request.queryParameters["eventId"].toString()

                val minimalUserResponseList: List<MinimalUserResponse> = EventHandlerService().getMutualFollowersPresences(id, accountId)
                return@get call.respond(minimalUserResponseList)
            }

            // Responsible for returning all public presences from the event
            get("/all") {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val id = call.request.queryParameters["eventId"].toString()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

                val allPresenceFromTheEvent: GenericResponseWithPagination<MinimalUserResponse> = EventHandlerService().getAllPresencesFromTheEvent(
                    eventId = id,
                    userId = accountId,
                    pageNumber = page,
                    pageSize = size
                )

                return@get call.respond(allPresenceFromTheEvent)

            }

        }

    }

    route("/comment") {

        authenticate("user-jwt", "moderator-jwt") {

            // Responsible for adding comment
            post {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val commentRequest: CommentRequest = call.receive<CommentRequest>()

                val commentResponse: CommentResponse = EventHandlerService().addComment(accountId, commentRequest)
                return@post call.respond(HttpStatusCode.Created, commentResponse)
            }

            // Responsible for deleting comment
            delete {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val commentId: String = call.parameters["id"].toString()
                EventHandlerService().deleteComment(commentId, accountId)
                return@delete call.respond(HttpStatusCode.OK)
            }

            // Responsible for up vote
            post("/up") {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val commentId: String = call.parameters["commentId"].toString()
                EventHandlerService().upComment(commentId, accountId)
                return@post call.respond(HttpStatusCode.OK)
            }

            // Responsible for down vote
            delete("/up") {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val commentId: String = call.parameters["commentId"].toString()
                EventHandlerService().downComment(commentId, accountId)
                return@delete call.respond(HttpStatusCode.OK)
            }

        }

        // Responsible for returning comments
        get {
            val id = call.request.queryParameters["eventId"].toString()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

            val commentResponseList: GenericResponseWithPagination<CommentResponse> = EventHandlerService().getCommentsFromTheEvent(id, size, page)
            return@get call.respond(commentResponseList)
        }

        // Responsible for returning parent comments
        get("/parent") {
            val eventId = call.request.queryParameters["eventId"].toString()
            val parentId = call.request.queryParameters["parentId"].toString()
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

            val commentResponseList: GenericResponseWithPagination<CommentResponse> = EventHandlerService().getParentComments(eventId, parentId, size, page)
            return@get call.respond(commentResponseList)
        }
    }

}
