package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class MinimalEventResponse(
    val name: String,
    val description: String,
    val image: String,
    val presences: Int,
    val type: List<String>,
    val date: String
)
