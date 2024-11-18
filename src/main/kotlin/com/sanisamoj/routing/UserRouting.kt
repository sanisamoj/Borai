package com.sanisamoj.routing

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.LoginRequest
import com.sanisamoj.data.models.dataclass.LoginResponse
import com.sanisamoj.data.models.dataclass.PutUserProfile
import com.sanisamoj.data.models.dataclass.UpdatePhoneWithValidationCode
import com.sanisamoj.data.models.dataclass.UserCreateRequest
import com.sanisamoj.data.models.dataclass.UserResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.services.followers.FollowerService
import com.sanisamoj.services.user.UserAuthenticationService
import com.sanisamoj.services.user.UserManagerService
import com.sanisamoj.services.user.UserService
import com.sanisamoj.utils.converters.BytesConverter
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.userRouting() {
    route("/user") {

        rateLimit(RateLimitName("register")) {

            // Route responsible for creating a user
            post {
                val user: UserCreateRequest = call.receive<UserCreateRequest>()
                val userResponse: UserResponse = UserService().createUser(user)
                return@post call.respond(userResponse)
            }

        }

        rateLimit(RateLimitName("validation")) {

            authenticate("user-jwt") {

                // Responsible for updating user profile
                put("/profile") {
                    val putUserProfile: PutUserProfile = call.receive()
                    val principal: JWTPrincipal = call.principal()!!
                    val userId: String = principal.payload.getClaim("id").asString()

                    val userManagerService = UserManagerService()

                    when {
                        putUserProfile.name != null -> {
                            val userResponse = userManagerService.updateName(userId, putUserProfile.name)
                            return@put call.respond(HttpStatusCode.OK, userResponse)
                        }
                        putUserProfile.phone != null -> {
                            userManagerService.updatePhoneProcess(userId, putUserProfile.phone)
                            return@put call.respond(HttpStatusCode.OK)
                        }
                        else -> {
                            return@put call.respond(HttpStatusCode.BadRequest, "No valid data to update")
                        }
                    }
                }

                // Confirm and update phone
                post("/phone") {
                    val updatePhoneWithValidationCode: UpdatePhoneWithValidationCode = call.receive<UpdatePhoneWithValidationCode>()
                    val principal: JWTPrincipal = call.principal()!!
                    val userId: String = principal.payload.getClaim("id").asString()

                    UserManagerService().validateValidationCodeToUpdatePhone(
                        userId = userId,
                        newPhone = updatePhoneWithValidationCode.phone,
                        validationCode = updatePhoneWithValidationCode.validationCode
                    )

                    return@post call.respond(HttpStatusCode.OK)
                }

                // Responsible for update image profile
                put("/image-profile") {
                    val principal: JWTPrincipal = call.principal()!!
                    val userId: String = principal.payload.getClaim("id").asString()

                    val multipartData: MultiPartData = call.receiveMultipart()
                    val requestSize: String? = call.request.headers[HttpHeaders.ContentLength]
                    val requestSizeInMb: Double = BytesConverter(requestSize!!.toLong()).getInMegabyte()
                    if (requestSizeInMb > GlobalContext.MAX_HEADERS_SIZE) throw CustomException(Errors.TheLimitMaxImageAllowed)

                    val userResponse: UserResponse = UserManagerService().updateImageProfile(multipartData, userId)
                    return@put call.respond(userResponse)
                }
            }
        }

        authenticate("user-jwt") {

            post("/follow") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val followingId = call.parameters["followingId"]

                if(followingId == null) throw CustomException(Errors.InvalidParameters)

                FollowerService().addFollower(accountId, followingId)
                return@post call.respond(HttpStatusCode.OK)
            }

            delete("/follow") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val followingId = call.parameters["followingId"]

                if(followingId == null) throw CustomException(Errors.InvalidParameters)

                FollowerService().removeFollower(accountId, followingId)
                return@delete call.respond(HttpStatusCode.OK)
            }

        }


    }


    route("/authentication") {

        rateLimit(RateLimitName("validation")) {

            // Responsible for generate email token
            post("/generate") {
                val identification = call.request.queryParameters["identifier"].toString()
                UserAuthenticationService().generateValidationEmailToken(identification)
                return@post call.respond(HttpStatusCode.OK)
            }

        }

        rateLimit(RateLimitName("login")) {

            // Responsible for login
            post("/login") {
                val loginRequest = call.receive<LoginRequest>()
                val userResponse: LoginResponse = UserAuthenticationService().login(loginRequest)
                return@post call.respond(userResponse)
            }
        }

        // Responsible for activate account by token email
        get("/activate") {
            val token: String = call.parameters["token"].toString()

            try {
                UserAuthenticationService().activateAccountByToken(token)
                return@get call.respond(HttpStatusCode.OK)

            } catch (_: Throwable) {
                return@get call.respond(HttpStatusCode.Unauthorized)
            }
        }

        rateLimit(RateLimitName("lightweight")) {

            authenticate("user-jwt") {

                // Responsible for session
                post("/session") {
                    val principal = call.principal<JWTPrincipal>()!!
                    val accountId = principal.payload.getClaim("id").asString()
                    val userResponse: UserResponse = UserAuthenticationService().session(accountId)
                    return@post call.respond(userResponse)
                }

                // Responsible for sign out
                delete("/session") {
                    val principal = call.principal<JWTPrincipal>()!!
                    val accountId = principal.payload.getClaim("id").asString()
                    val sessionId = principal.payload.getClaim("session").asString()
                    UserAuthenticationService().signOut(accountId, sessionId)
                    return@delete call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}