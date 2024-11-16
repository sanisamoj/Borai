package com.sanisamoj.routing

import com.sanisamoj.data.models.dataclass.UserCreateRequest
import com.sanisamoj.data.models.dataclass.UserResponse
import com.sanisamoj.services.user.UserService
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.userRouting() {
    route("/user") {

        // Route responsible for creating a user
        post {
            val user: UserCreateRequest = call.receive<UserCreateRequest>()
            val userResponse: UserResponse = UserService().createUser(user)
            return@post call.respond(userResponse)
        }

    }
}