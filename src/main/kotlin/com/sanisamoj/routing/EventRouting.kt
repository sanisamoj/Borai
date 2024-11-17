package com.sanisamoj.routing

import com.sanisamoj.data.models.dataclass.CreateEventRequest
import com.sanisamoj.data.models.dataclass.EventResponse
import com.sanisamoj.services.event.EventService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.eventRouting() {

    route("event") {

        authenticate("user-jwt") {

            // Responsible for creating a event
            post {
                val principal: JWTPrincipal = call.principal()!!
                val accountId: String = principal.payload.getClaim("id").asString()
                val createEventRequest: CreateEventRequest = call.receive<CreateEventRequest>()
                val eventResponse: EventResponse = EventService().createEvent(accountId, createEventRequest)
                return@post call.respond(HttpStatusCode.Created, eventResponse)
            }

        }

    }

}