package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class CreateEventRequest(
    val name: String,
    val description: String,
    val image: String,
    val otherImages: List<String>? = null,
    val address: Address,
    val date: String,
    val type: List<String>
)
