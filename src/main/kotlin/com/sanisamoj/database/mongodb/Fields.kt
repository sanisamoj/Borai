package com.sanisamoj.database.mongodb

enum class Fields(val title: String) {
    Id(title = "_id"),
    Email(title = "email"),
    Phone(title = "phone"),
    ImageProfile(title = "imageProfile")
}