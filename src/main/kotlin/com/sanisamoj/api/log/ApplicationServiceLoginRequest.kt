package com.sanisamoj.api.log

import kotlinx.serialization.Serializable

@Serializable
data class ApplicationServiceLoginRequest(
    val applicationName: String,
    val password: String
)
