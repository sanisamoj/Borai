package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class Comment(
    @BsonId val id: ObjectId = ObjectId(),
    val eventId: String,
    val userId: String,
    val nick: String,
    val imageProfile: String,
    val text: String,
    val parentId: String? = null,
    val answersCount: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
