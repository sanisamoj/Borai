package com.sanisamoj.services.insignia

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.dataclass.CreateInsigniaRequest
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.Insignia
import com.sanisamoj.data.models.dataclass.InsigniaResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.InsigniaCriteriaType
import com.sanisamoj.data.models.interfaces.InsigniaRepository
import com.sanisamoj.utils.analyzers.isInEnum

class InsigniaHandler(
    private val insigniaObserver: InsigniaRepository = GlobalContext.getInsigniaObserver()
) {

    suspend fun createInsignia(createInsigniaRequest: CreateInsigniaRequest): InsigniaResponse {
        if(!createInsigniaRequest.criteria.isInEnum<InsigniaCriteriaType>()) throw CustomException(Errors.InvalidParameters)

        val insignia = Insignia(
            name = createInsigniaRequest.name,
            image = createInsigniaRequest.image,
            description = createInsigniaRequest.description,
            criteria = createInsigniaRequest.criteria,
            quantity = createInsigniaRequest.quantity
        )

        insigniaObserver.registerInsignia(insignia)
        return InsigniaFactory.insigniaResponseFactory(insignia)
    }

    suspend fun getAllInsignias(): List<InsigniaResponse> {
        val insignias: List<Insignia> = insigniaObserver.getAllInsignias()
        return insignias.map { InsigniaFactory.insigniaResponseFactory(it) }
    }
}