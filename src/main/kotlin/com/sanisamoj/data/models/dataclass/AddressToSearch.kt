package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class AddressToSearch(
    val street: String? = null,
    val neighborhood: String? = null,
    val city: String? = null,
    val uf: String? = null
)
