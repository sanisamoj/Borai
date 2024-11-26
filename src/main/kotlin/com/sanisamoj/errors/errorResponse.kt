package com.sanisamoj.errors

import com.sanisamoj.config.GlobalContext.MAX_HEADERS_SIZE
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.ErrorResponse
import com.sanisamoj.data.models.enums.ActionMessages
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.Infos
import com.sanisamoj.utils.converters.BytesConverter
import io.ktor.http.HttpStatusCode

fun errorResponse(exception: CustomException): Pair<HttpStatusCode, ErrorResponse> {
    val response = when (exception.error) {
        Errors.UserAlreadyExists -> {
            HttpStatusCode.Conflict to ErrorResponse(Errors.UserAlreadyExists.description)
        }

        Errors.DataIsMissing -> {
            HttpStatusCode.BadRequest to ErrorResponse(Errors.DataIsMissing.description)
        }

        Errors.UserNotFound -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.UserNotFound.description)
        }

        Errors.InvalidLogin -> {
            HttpStatusCode.Unauthorized to ErrorResponse(Errors.InvalidLogin.description)
        }

        Errors.UnableToComplete -> {
            HttpStatusCode.Forbidden to ErrorResponse(Errors.UnableToComplete.description)
        }

        Errors.ExpiredValidationCode -> {
            HttpStatusCode.Forbidden to ErrorResponse(Errors.ExpiredValidationCode.description)
        }

        Errors.InvalidValidationCode -> {
            HttpStatusCode.Forbidden to ErrorResponse(Errors.InvalidValidationCode.description)
        }

        Errors.PresenceAlreadyMarked -> {
            HttpStatusCode.Conflict to ErrorResponse(Errors.PresenceAlreadyMarked.description)
        }

        Errors.UserIsNotPresentInTheListOfFollowers -> {
            HttpStatusCode.UnprocessableEntity to ErrorResponse(Errors.UserIsNotPresentInTheListOfFollowers.description)
        }

        Errors.UserIsAlreadyOnTheFollowersList -> {
            HttpStatusCode.UnprocessableEntity to ErrorResponse(Errors.UserIsAlreadyOnTheFollowersList.description)
        }

        Errors.CommentsCannotExceedLevelOneResponses -> {
            HttpStatusCode.UnprocessableEntity to ErrorResponse(Errors.CommentsCannotExceedLevelOneResponses.description)
        }

        Errors.FollowRequestAlreadyExists -> {
            HttpStatusCode.Conflict to ErrorResponse(Errors.FollowRequestAlreadyExists.description)
        }

        Errors.UserIsNotPresentInTheListOfFollowing -> {
            HttpStatusCode.UnprocessableEntity to ErrorResponse(Errors.UserIsNotPresentInTheListOfFollowing.description)
        }

        Errors.FollowerNotFound -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.FollowerNotFound.description)
        }

        Errors.ProfileIsPrivate -> {
            HttpStatusCode.Forbidden to ErrorResponse(Errors.ProfileIsPrivate.description)
        }

        Errors.TheEventHasAnotherOwner -> {
            HttpStatusCode.Forbidden to ErrorResponse(Errors.TheEventHasAnotherOwner.description)
        }

        Errors.InvalidParameters -> {
            HttpStatusCode.BadRequest to ErrorResponse(Errors.InvalidParameters.description)
        }

        Errors.InactiveAccount -> {
            HttpStatusCode.Forbidden to ErrorResponse(
                error = Errors.InactiveAccount.description,
                details = ActionMessages.ActivateAccount.description
            )
        }

        Errors.BlockedAccount -> {
            HttpStatusCode.Forbidden to ErrorResponse(
                error = Errors.BlockedAccount.description,
                details = ActionMessages.ContactSupport.description
            )
        }

        Errors.TheLimitMaxImageAllowed -> {
            HttpStatusCode.BadRequest to ErrorResponse(
                error = Errors.TheLimitMaxImageAllowed.description,
                details = "${Infos.MaximumMediaSizeAllowedIs.description} ${BytesConverter(MAX_HEADERS_SIZE.toLong()).getInMegabyte()}mb."
            )
        }

        else -> {
            HttpStatusCode.InternalServerError to ErrorResponse(
                error = Errors.InternalServerError.description,
                details = exception.additionalInfo
            )
        }
    }

    return response
}