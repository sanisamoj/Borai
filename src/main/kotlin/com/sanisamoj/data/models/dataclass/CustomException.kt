package com.sanisamoj.data.models.dataclass

import com.sanisamoj.data.models.enums.Errors

class CustomException(
    val error: Errors,
    val additionalInfo: String? = null
) : RuntimeException(error.description)