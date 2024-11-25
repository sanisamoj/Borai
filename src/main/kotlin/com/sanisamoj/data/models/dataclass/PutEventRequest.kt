package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class PutEventRequest(
    val name: String? = null,
    val description: String? = null,
    val address: Address? = null,
    val date: String? = null,
    val type: List<String>? = null,
    val status: String? = null
)
