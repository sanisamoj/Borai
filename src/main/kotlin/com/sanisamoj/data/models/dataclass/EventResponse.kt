package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class EventResponse(
    val id: String,
    val eventCreator: MinimalUserResponse,
    val name: String,
    val description: String,
    val image: String,
    val otherImages: List<String>,
    val address: Address,
    val date: String,
    val presences: Int,
    val type: List<String>,
    val status: String,
    val createdAt: String
)
