package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class EventVote(
    val eventId: String,
    val userId: String = "",
    val rating: Int,
    val createdAt: String = LocalDateTime.now().toString()
)
