package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class User(
    @BsonId val id: ObjectId = ObjectId(),
    val username: String,
    val imageProfile: String,
    val email: String,
    val phone: String,
    val password: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
