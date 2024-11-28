package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class UserForModeratorResponse(
    val id: String,
    val nick: String,
    val bio: String? = null,
    val username: String,
    val imageProfile: String? = null,
    val email: String,
    val phone: String,
    val address: Address? = null,
    val events: List<MinimalEventResponse>,
    val presences: List<MinimalEventResponse>,
    val createdAt: String
)
