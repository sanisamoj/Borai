package com.sanisamoj.data.models.dataclass

import com.sanisamoj.data.models.enums.AccountType
import kotlinx.serialization.Serializable

@Serializable
data class UserCreateRequest(
    val nick: String,
    val bio: String? = null,
    val username: String,
    val imageProfile: String? = null,
    val email: String,
    val password: String,
    val phone: String,
    val type: String = AccountType.PARTICIPANT.name,
    val doc: Doc? = null,
    val address: Address? = null
)
