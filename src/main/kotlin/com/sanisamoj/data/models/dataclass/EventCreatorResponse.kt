package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class EventCreatorResponse(
    val id: String,
    val nick: String,
    val imageProfile: String,
    val accountType: String,
)
