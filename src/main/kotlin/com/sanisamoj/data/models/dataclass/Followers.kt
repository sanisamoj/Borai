package com.sanisamoj.data.models.dataclass

data class Followers(
    val accepted: List<String> = listOf(),
    val pending: List<String> = listOf()
)
