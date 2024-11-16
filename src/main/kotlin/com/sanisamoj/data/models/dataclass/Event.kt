package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class Event(
    @BsonId val id: ObjectId = ObjectId(),
    val name: String,
    val description: String,
    val imageUrl: String,
    val localization: String,
    val date: String,
    val type: String,
    val comments: List<String>,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
