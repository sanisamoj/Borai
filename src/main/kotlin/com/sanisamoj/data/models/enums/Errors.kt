package com.sanisamoj.data.models.enums

enum class Errors(val description: String) {
    MaxRetriesReached("Max retries reached, token update failed."),
    InternalServerError("Internal Server Error!"),
    BotTokenNotUpdated("Bot token not updated!"),
    LogTokenNotUpdated("Log token not updated!"),
    RedisNotResponding("Redis not responding!"),
    UserAlreadyExists("User already exists!"),
    TooManyRequests("Too many requests!"),
    DataIsMissing("Data is missing"),
    UserNotFound("User Not Found!")
}