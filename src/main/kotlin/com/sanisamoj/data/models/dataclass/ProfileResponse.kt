package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class ProfileResponse(
    val id: String,
    val nick: String,
    val bio: String?,
    val imageProfile: String,
    val type: String,
    val events: Int,
    val followers: Int,
    val following: Int,
    val public: Boolean
)
