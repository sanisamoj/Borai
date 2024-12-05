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
    val type: String,
    val address: Address,
    val presences: Int,
    val followers: Int,
    val following: Int,
    val insignias: List<InsigniaResponse>?,
    val visibleInsignias: List<InsigniaResponse>?,
    val preferences: UserPreference?,
    val createdAt: String
)