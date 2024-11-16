package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Presence(
    @BsonId val id: ObjectId = ObjectId(),
    val eventId: String,
    val userId: String,
    val status: String,
    val createdAt: String
)
