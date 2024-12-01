package com.sanisamoj.pluguins

import com.sanisamoj.routing.eventRouting
import com.sanisamoj.routing.moderatorRouting
import com.sanisamoj.routing.profileRouting
import com.sanisamoj.routing.userRouting
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        userRouting()
        eventRouting()
        moderatorRouting()
        profileRouting()
    }
}
