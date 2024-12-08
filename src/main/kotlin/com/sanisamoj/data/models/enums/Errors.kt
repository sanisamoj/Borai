package com.sanisamoj.data.models.enums

enum class Errors(val description: String) {
    UserIsNotPresentInTheListOfFollowers("User is not present in the list of followers!"),
    UserIsNotPresentInTheListOfFollowing("User is not present in the list of following!"),
    CommentsCannotExceedLevelOneResponses("Comments cannot exceed level one responses!"),
    TheLimiteVisibleInsigniaReached("The limit of visible insignia has been reached."),
    CannotRemoveUpIfNotMade("Cannot remove upvote because the user has not upvoted!"),
    UserIsAlreadyOnTheFollowersList("User is already on the followers list."),
    LimitOnTheNumberOfImageReached("Limit on the number of images reached."),
    UserDidNotAttendEvent("User did not attend the event and cannot vote!"),
    ImageNotFoundInOtherImages("Image not found in event's other images!"),
    TheEventDateCannotBeInThePast("The event date cannot be in the past."),
    TheLimitMaxImageAllowed("The limit of images has been exceeded!"),
    InsigniaNotFoundInUserList("Insignia not found in user list."),
    MaxRetriesReached("Max retries reached, token update failed."),
    FollowRequestAlreadyExists("Follow request already exists!"),
    UserHasAlreadyUpvoted("User has already upvoted this item!"),
    UserAlreadyVoted("User has already voted for this event!"),
    EventNotEnded("You can only vote after the event ends."),
    TheEventHasAnotherOwner("The event has another owner!"),
    InvalidValidationCode("Invalid Validation Code!"),
    PresenceAlreadyMarked("Presence already marked!"),
    ExpiredValidationCode("Expired Validation Code!"),
    InvalidRating("Rating must be between 1 and 5."),
    UnsupportedMediaType("Unsupported media type!"),
    InsigniaAlreadyAdded("Insignia already added!"),
    InternalServerError("Internal Server Error!"),
    BotTokenNotUpdated("Bot token not updated!"),
    DuplicatePreference("Duplicate preference!"),
    LogTokenNotUpdated("Log token not updated!"),
    NoItemsWereDeleted("No items were deleted!"),
    RedisNotResponding("Redis not responding!"),
    UserAlreadyExists("User already exists!"),
    InvalidParameters("Invalid parameters!"),
    UnableToComplete("Unable to complete!"),
    FollowerNotFound("Follower not found!"),
    ProfileIsPrivate("Profile is private!"),
    InvalidLogin("Invalid email/password!"),
    PresenceNotFound("Presence not found!"),
    CommentNotFound("Comment not found!"),
    TooManyRequests("Too many requests!"),
    InactiveAccount("Inactive Account!"),
    ExpiredSession("Expired session!"),
    BlockedAccount("Blocked Account!"),
    EventNotFound("Event not found!"),
    MediaNotExist("Media not exist!"),
    DataIsMissing("Data is missing"),
    UserNotFound("User Not Found!"),
    NotFound("Not found!")
}