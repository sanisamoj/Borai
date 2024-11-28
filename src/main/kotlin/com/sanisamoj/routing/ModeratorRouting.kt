package com.sanisamoj.routing

import com.sanisamoj.data.models.dataclass.GenericResponseWithPagination
import com.sanisamoj.data.models.dataclass.UserForModeratorResponse
import com.sanisamoj.services.moderator.ModeratorActivityService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlin.text.toIntOrNull

fun Route.moderatorRouting() {

    route("/moderator") {

        authenticate("moderator-jwt") {

            // Responsible for returning users
            get("/users") {
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25
                val userForModeratorResponseList: GenericResponseWithPagination<UserForModeratorResponse>
                        = ModeratorActivityService().getUsersWithPagination(page, size)

                return@get call.respond(userForModeratorResponseList)
            }

            // Responsible for deleting a event
            delete("/event") {
                val eventId: String = call.parameters["eventId"].toString()
                ModeratorActivityService().deleteEvent(eventId)
                return@delete call.respond(HttpStatusCode.OK)
            }

        }

    }

}