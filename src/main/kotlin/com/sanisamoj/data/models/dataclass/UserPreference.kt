package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class UserPreference(
    val eventPreferences: List<String> = listOf(),
    val creators: List<String> = listOf(),
)
