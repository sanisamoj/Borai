package com.sanisamoj.routing

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.services.followers.FollowerService
import com.sanisamoj.services.user.UserActivityService
import com.sanisamoj.services.user.UserAuthenticationService
import com.sanisamoj.services.user.UserHandlerService
import com.sanisamoj.services.user.UserManagerService
import com.sanisamoj.services.user.UserService
import com.sanisamoj.utils.converters.BytesConverter
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

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
                val followingId = call.parameters["followingId"].toString()

                FollowerService().cancelFollowRequest(accountId, followingId)
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

            // Responsible for returning a profile from the user
            get {
                val profileId = call.parameters["id"].toString()
                val profileResponse: ProfileResponse = UserActivityService().getProfileById(profileId)
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