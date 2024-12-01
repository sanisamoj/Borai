package com.sanisamoj.data.models.dataclass

import java.time.LocalDateTime

data class SearchEventFilters(
    val name: String? = null,
    val nick: String? = null,
    val address: AddressToSearch? = null,
    val type: List<String>? = null,
    val status: String? = null,
    val date: LocalDateTime? = null,
    val endDate: LocalDateTime? = null,
    val page: Int = 1,
    val size: Int = 25
)
