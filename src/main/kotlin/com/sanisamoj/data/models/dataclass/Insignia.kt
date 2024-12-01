package com.sanisamoj.data.models.dataclass

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class Insignia(
    @BsonId val id: ObjectId = ObjectId(),
    val name: String,
    val image: String,
    val description: String,
    val criteria: String,
    val quantity: Double,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
