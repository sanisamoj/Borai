package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class Event(
    @BsonId val id: ObjectId = ObjectId(),
    val promoterId: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val otherImages: List<String>,
    val address: Address,
    val date: LocalDateTime,
    val presences: Int = 0,
    val type: List<String>,
    val status: String,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
