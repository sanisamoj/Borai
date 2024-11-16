package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class Doc(
    val type: String,
    val number: String
)
