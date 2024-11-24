package com.sanisamoj.routing

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.GenericResponseWithPagination
import com.sanisamoj.data.models.dataclass.LoginRequest
import com.sanisamoj.data.models.dataclass.LoginResponse
import com.sanisamoj.data.models.dataclass.MinimalEventResponse
import com.sanisamoj.data.models.dataclass.MinimalUserResponse
import com.sanisamoj.data.models.dataclass.PutUserProfile
import com.sanisamoj.data.models.dataclass.UpdatePhoneWithValidationCode
import com.sanisamoj.data.models.dataclass.UserCreateRequest
import com.sanisamoj.data.models.dataclass.UserResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.services.followers.FollowerService
import com.sanisamoj.services.user.UserActivityService
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
                        putUserProfile.bio != null -> {
                            userManagerService.updateBio(userId, putUserProfile.bio)
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

            // Responsible for sending a request to follow
            post("/follow") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val followingId = call.parameters["followingId"]

                if(followingId == null) throw CustomException(Errors.InvalidParameters)

                FollowerService().sendFollowRequest(accountId, followingId)
                return@post call.respond(HttpStatusCode.OK)
            }

            // Responsible for removing a follow
            delete("/follow") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val followingId = call.parameters["followingId"]

                if(followingId == null) throw CustomException(Errors.InvalidParameters)

                FollowerService().removeFollowing(accountId, followingId)
                return@delete call.respond(HttpStatusCode.OK)
            }

            // Responsible for accepting a request
            post("/follow/accept") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val followerId = call.parameters["followerId"].toString()

                FollowerService().acceptFollowRequest(followerId, accountId)
                return@post call.respond(HttpStatusCode.OK)
            }

            // Responsible for rejecting a request
            post("/follow/reject") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val followerId = call.parameters["followerId"].toString()

                FollowerService().rejectFollowRequest(accountId, followerId)
                return@post call.respond(HttpStatusCode.OK)
            }

            // Responsible for canceling a request
            post("/follow/cancel") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val followerId = call.parameters["followerId"].toString()

                FollowerService().cancelFollowRequest(accountId, followerId)
                return@post call.respond(HttpStatusCode.OK)
            }

            // Responsible for returning pending follow requests
            get("/follow/pending") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val minimalUserResponseList: List<MinimalUserResponse> = FollowerService().getPendingFollowRequests(accountId)
                return@get call.respond(minimalUserResponseList)
            }

            // Responsible for returning pending sent follow requests
            get("/follow/pending/sent") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val minimalUserResponseList: List<MinimalUserResponse> = FollowerService().getPendingSentRequests(accountId)
                return@get call.respond(minimalUserResponseList)
            }

        }

    }

    route("/profile") {

        authenticate("user-jwt") {

            // Responsible for returning events in which the user was present
            get("/presences") {
                val principal = call.principal<JWTPrincipal>()!!
                val accountId = principal.payload.getClaim("id").asString()
                val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
                val size = call.request.queryParameters["size"]?.toIntOrNull() ?: 25

                val minimalEventResponseList = UserActivityService().getPresenceByUser(accountId, size, page)
                return@get call.respond(minimalEventResponseList)
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