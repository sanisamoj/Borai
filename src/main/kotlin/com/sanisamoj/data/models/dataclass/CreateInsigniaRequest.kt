package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class CreateInsigniaRequest(
    val name: String,
    val image: String,
    val description: String,
    val criteria: String,
    val quantity: Double
)
