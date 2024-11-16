package com.sanisamoj.data.models.dataclass

import kotlinx.serialization.Serializable

@Serializable
data class GlobalWarnings(
    val systemName: String,
    val welcome: String,
    val activateYourAccount: String,
    val thisYourValidationCode: String
)
