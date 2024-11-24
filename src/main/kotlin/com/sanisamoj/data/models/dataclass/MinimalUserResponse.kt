package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class MinimalUserResponse(
    val id: String,
    val nick: String,
    val bio: String? = null,
    val imageProfile: String,
    val accountType: String,
)
