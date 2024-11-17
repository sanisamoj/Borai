package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class AddressToSearch(
    val street: String?,
    val neighborhood: String?,
    val city: String?,
    val uf: String?
)
