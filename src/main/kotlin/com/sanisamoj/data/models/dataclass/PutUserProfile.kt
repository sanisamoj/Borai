package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class PutUserProfile(
    val bio: String? = null,
    val name: String? = null,
    val nick: String? = null,
    val phone: String? = null,
    val address: Address? = null,
    val password: String? = null,
    val validationCode: Int? = null
)
