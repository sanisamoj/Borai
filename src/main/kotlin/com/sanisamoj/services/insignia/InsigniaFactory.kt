package com.sanisamoj.services.insignia

import com.sanisamoj.data.models.dataclass.Insignia
import com.sanisamoj.data.models.dataclass.InsigniaResponse

object InsigniaFactory {

    fun insigniaResponseFactory(insignia: Insignia): InsigniaResponse {
        return InsigniaResponse(
            id = insignia.id.toString(),
            image = insignia.image,
            description = insignia.description,
            criteria = insignia.criteria,
            quantity = insignia.quantity,
            createdAt = insignia.createdAt.toString()
        )
    }

}