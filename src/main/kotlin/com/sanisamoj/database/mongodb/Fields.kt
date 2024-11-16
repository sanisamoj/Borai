package com.sanisamoj.database.mongodb

enum class Fields(val title: String) {
    Id(title = "_id"),
    Username(title = "username"),
    Email(title = "email"),
    Phone(title = "phone"),
    ImageProfile(title = "imageProfile"),
    AccountStatus(title = "accountStatus"),
    ValidationCode(title = "validationCode")
}