package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val nick: String,
    val bio: String? = null,
    val username: String,
    val imageProfile: String? = null,
    val email: String,
    val phone: String,
    val createdAt: String
)