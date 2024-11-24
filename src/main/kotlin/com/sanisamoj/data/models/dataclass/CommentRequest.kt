package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class CommentRequest(
    val eventId: String,
    val comment: String,
    val parentId: String? = null
)
