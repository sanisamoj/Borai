package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class CommentResponse(
    val id: String,
    val eventId: String,
    val userId: String,
    val nick: String,
    val imageProfile: String?,
    val comment: String,
    val parentId: String?,
    val answersCount: Int,
    val createdAt: String
)
