package com.sanisamoj.data.models.enums

enum class Errors(val description: String) {
    LimitOnTheNumberOfImageReached("Limit on the number of images reached."),
    TheLimitMaxImageAllowed("The limit of images has been exceeded!"),
    MaxRetriesReached("Max retries reached, token update failed."),
    InvalidValidationCode("Invalid Validation Code!"),
    PresenceAlreadyMarked("Presence already marked!"),
    ExpiredValidationCode("Expired Validation Code!"),
    UnsupportedMediaType("Unsupported media type!"),
    InternalServerError("Internal Server Error!"),
    BotTokenNotUpdated("Bot token not updated!"),
    LogTokenNotUpdated("Log token not updated!"),
    NoItemsWereDeleted("No items were deleted!"),
    RedisNotResponding("Redis not responding!"),
    UserAlreadyExists("User already exists!"),
    InvalidParameters("Invalid parameters!"),
    UnableToComplete("Unable to complete!"),
    InvalidLogin("Invalid email/password!"),
    PresenceNotFound("Presence not found!"),
    TooManyRequests("Too many requests!"),
    InactiveAccount("Inactive Account!"),
    ExpiredSession("Expired session!"),
    BlockedAccount("Blocked Account!"),
    EventNotFound("Event not found!"),
    MediaNotExist("Media not exist!"),
    DataIsMissing("Data is missing"),
    UserNotFound("User Not Found!")
}