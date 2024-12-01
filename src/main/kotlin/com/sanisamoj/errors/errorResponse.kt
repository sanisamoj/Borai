package com.sanisamoj.errors

import com.sanisamoj.config.GlobalContext.MAX_HEADERS_SIZE
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.ErrorResponse
import com.sanisamoj.data.models.enums.ActionMessages
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.Infos
import com.sanisamoj.utils.converters.BytesConverter
import io.ktor.http.*

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

        Errors.EventNotEnded -> {
            HttpStatusCode.Forbidden to ErrorResponse(Errors.EventNotEnded.description)
        }

        Errors.UserAlreadyVoted -> {
            HttpStatusCode.Conflict to ErrorResponse(Errors.UserAlreadyVoted.description)
        }

        Errors.InvalidRating -> {
            HttpStatusCode.BadRequest to ErrorResponse(Errors.InvalidRating.description)
        }

        Errors.UserDidNotAttendEvent -> {
            HttpStatusCode.Forbidden to ErrorResponse(Errors.UserDidNotAttendEvent.description)
        }

        Errors.CannotRemoveUpIfNotMade -> {
            HttpStatusCode.UnprocessableEntity to ErrorResponse(Errors.CannotRemoveUpIfNotMade.description)
        }

        Errors.UserHasAlreadyUpvoted -> {
            HttpStatusCode.Conflict to ErrorResponse(Errors.UserHasAlreadyUpvoted.description)
        }

        Errors.NotFound -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.NotFound.description)
        }

        Errors.EventNotFound -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.EventNotFound.description)
        }

        Errors.ImageNotFoundInOtherImages -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.ImageNotFoundInOtherImages.description)
        }

        Errors.LimitOnTheNumberOfImageReached -> {
            HttpStatusCode.UnprocessableEntity to ErrorResponse(Errors.LimitOnTheNumberOfImageReached.description)
        }

        Errors.InsigniaNotFoundInUserList -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.InsigniaNotFoundInUserList.description)
        }

        Errors.MaxRetriesReached -> {
            HttpStatusCode.UnprocessableEntity to ErrorResponse(Errors.MaxRetriesReached.description)
        }

        Errors.PresenceNotFound -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.PresenceNotFound.description)
        }

        Errors.CommentNotFound -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.CommentNotFound.description)
        }

        Errors.MediaNotExist -> {
            HttpStatusCode.NotFound to ErrorResponse(Errors.MediaNotExist.description)
        }

        Errors.InsigniaAlreadyAdded -> {
            HttpStatusCode.Conflict to ErrorResponse(Errors.InsigniaAlreadyAdded.description)
        }

        Errors.UnsupportedMediaType -> {
            HttpStatusCode.UnsupportedMediaType to ErrorResponse(
                error = Errors.UnsupportedMediaType.description,
                details = ActionMessages.MimeTypesAllowed.description
            )
        }

        Errors.TheLimiteVisibleInsigniaReached -> {
            HttpStatusCode.UnprocessableEntity to ErrorResponse(
                error = Errors.TheLimiteVisibleInsigniaReached.description,
                details = Infos.VisibleLimitInsignias.description
            )
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