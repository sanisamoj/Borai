package com.sanisamoj.routing

import com.sanisamoj.data.models.dataclass.CreateInsigniaRequest
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.EventResponse
import com.sanisamoj.data.models.dataclass.InsigniaResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.repository.InsigniaObserver
import com.sanisamoj.services.event.EventHandlerService
import com.sanisamoj.services.insignia.InsigniaHandler
import com.sanisamoj.services.user.UserActivityService
import com.sanisamoj.services.user.UserHandlerService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.profileRouting() {

    route("/profile") {

        authenticate("user-jwt") {

            // Responsible for returning a profile from the user
            get {
                val profileId = call.parameters["id"]
                val profileNick = call.parameters["nick"]

                val profileResponse: Any = when {
                    profileId != null -> UserActivityService().getProfileById(profileId)
                    profileNick != null -> UserActivityService().getProfilesByNick(profileNick)
                    else -> { throw CustomException(Errors.InvalidParameters) }
                }

                return@get call.respond(profileResponse)
            }

            // Responsible for returning events in which the user was present
            get("/presences") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

                val minimalEventResponseList = UserHandlerService().getPresenceByUser(accountId, size, page)
                return@get call.respond(minimalEventResponseList)
            }

            // Responsible for returning followers
            get("/followers") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

                val minimalUserResponseList = UserHandlerService().getFollowers(accountId, size, page)
                return@get call.respond(minimalUserResponseList)
            }

            // Responsible for returning following
            get("/following") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

                val minimalUserResponseList = UserHandlerService().getFollowing(accountId, size, page)
                return@get call.respond(minimalUserResponseList)
            }

            // Responsible for returning presences from profile
            get("/other-presences") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val profileId = call.request.queryParameters["id"].toString()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

                val minimalUserResponseList = UserActivityService().getPresencesFromProfile(
                    userId = accountId,
                    profileId = profileId,
                    page = page,
                    size = size
                )

                return@get call.respond(minimalUserResponseList)
            }

            // Responsible for returning presences from profile
            get("/other-events") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val profileId = call.request.queryParameters["profileId"].toString()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

                val minimalUserResponseList = UserActivityService().getEventsFromProfile(
                    userId = accountId,
                    profileId = profileId,
                    pageNumber = page,
                    pageSize = size
                )

                return@get call.respond(minimalUserResponseList)
            }

            //Responsible for returning own event
            get("/events") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val all: Boolean = call.request.queryParameters["all"] == "true"
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

                if(all) {
                    val eventResponseList:  List<EventResponse> = EventHandlerService().getAllEventsFromAccount(accountId)
                    return@get call.respond(eventResponseList)
                } else {
                    val eventResponseListWithPagination = EventHandlerService().getEventsFromAccount(accountId, size, page)
                    return@get call.respond(eventResponseListWithPagination)
                }

            }

        }

    }

    route("/insignias") {

        authenticate("moderator-jwt") {

            // Responsible for creating a insignia
            post {
                val createInsigniaRequest: CreateInsigniaRequest = call.receive()
                val insigniaResponse: InsigniaResponse = InsigniaHandler().createInsignia(createInsigniaRequest)
                return@post call.respond(HttpStatusCode.Created, insigniaResponse)
            }

        }

        authenticate("user-jwt") {

            // Responsible for add visible insignia to the profile
            put("/visible") {
                val principal: JWTPrincipal = call.principal<JWTPrincipal>()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val insigniaId: String = call.parameters["insigniaId"].toString()
                InsigniaObserver.addVisibleInsignia(accountId, insigniaId)
                return@put call.respond(HttpStatusCode.OK)
            }

            // Responsible for remove visible insignia to the profile
            put("invisible") {
                val principal: JWTPrincipal = call.principal<JWTPrincipal>()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val insigniaId: String = call.request.queryParameters["insigniaId"].toString()
                InsigniaObserver.removeVisibleInsignia(accountId, insigniaId)
                return@put call.respond(HttpStatusCode.OK)
            }

        }

        // Responsible for returning all insignias
        get {
            val insigniasResponseList: List<InsigniaResponse> = InsigniaHandler().getAllInsignias()
            return@get call.respond(insigniasResponseList)
        }

    }

}