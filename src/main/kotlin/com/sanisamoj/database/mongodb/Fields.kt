package com.sanisamoj.database.mongodb

enum class Fields(val title: String) {
    Id(title = "_id"),
    Nick(title = "nick"),
    Name(title = "name"),
    Date(title = "date"),
    Type(title = "type"),
    Email(title = "email"),
    Phone(title = "phone"),
    Status(title = "status"),
    EventId(title = "eventId"),
    ParentId(title = "parentId"),
    Username(title = "username"),
    AccountId(title = "accountId"),
    Presences(title = "presences"),
    PromoterId(title = "promoterId"),
    ImageProfile(title = "imageProfile"),
    AccountStatus(title = "accountStatus"),
    ValidationCode(title = "validationCode")
}