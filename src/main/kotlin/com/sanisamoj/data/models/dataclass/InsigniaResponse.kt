package com.sanisamoj.data.models.dataclass
import kotlinx.serialization.Serializable

@Serializable
data class InsigniaResponse(
    val id: String,
    val image: String,
    val description: String,
    val criteria: String,
    val quantity: Double,
    val createdAt: String
)