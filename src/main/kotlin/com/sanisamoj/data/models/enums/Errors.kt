package com.sanisamoj.data.models.enums

enum class Errors(val description: String) {
    TheLimitMaxImageAllowed("The limit of images has been exceeded!"),
    MaxRetriesReached("Max retries reached, token update failed."),
    InvalidValidationCode("Invalid Validation Code!"),
    ExpiredValidationCode("Expired Validation Code!"),
    InternalServerError("Internal Server Error!"),
    BotTokenNotUpdated("Bot token not updated!"),
    LogTokenNotUpdated("Log token not updated!"),
    RedisNotResponding("Redis not responding!"),
    UserAlreadyExists("User already exists!"),
    UnableToComplete("Unable to complete!"),
    InvalidLogin("Invalid email/password!"),
    TooManyRequests("Too many requests!"),
    InactiveAccount("Inactive Account!"),
    ExpiredSession("Expired session!"),
    BlockedAccount("Blocked Account!"),
    EventNotFound("Event not found!"),
    DataIsMissing("Data is missing"),
    UserNotFound("User Not Found!")
}