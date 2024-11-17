package com.sanisamoj.data.models.dataclass

import com.sanisamoj.data.models.enums.EventStatus
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class Event(
    @BsonId val id: ObjectId = ObjectId(),
    val accountId: String,
    val name: String,
    val description: String,
    val image: String,
    val otherImages: List<String>,
    val address: Address,
    val date: LocalDateTime,
    val presences: Int = 0,
    val type: List<String>,
    val status: String = EventStatus.SCHEDULED.name,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
