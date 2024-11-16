package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Comments(
    @BsonId val id: ObjectId = ObjectId(),
    val eventId: String,
    val userId: String,
    val comment: String,
    val createdAt: String
)
