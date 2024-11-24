package com.sanisamoj.data.models.dataclass

import com.sanisamoj.data.models.enums.PresenceStatus
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class Presence(
    @BsonId val id: ObjectId = ObjectId(),
    val eventId: String,
    val userId: String,
    val nick: String,
    val status: String = PresenceStatus.MARKED_PRESENT.name,
    val imageProfile: String,
    val accountType: String,
    val accountIsPublic: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
