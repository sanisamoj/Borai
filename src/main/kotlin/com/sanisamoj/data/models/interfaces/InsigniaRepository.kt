package com.sanisamoj.data.models.interfaces

import com.sanisamoj.data.models.dataclass.Insignia
import com.sanisamoj.data.models.dataclass.InsigniaPoints
import com.sanisamoj.data.models.enums.InsigniaCriteriaType

interface InsigniaRepository {
    suspend fun registerInsignia(insignia: Insignia): Insignia
    suspend fun getAllInsignias(): List<Insignia>
    suspend fun addPoints(userId: String, insigniaCriteriaType: InsigniaCriteriaType, points: Double)
    suspend fun removePoints(userId: String, insigniaCriteriaType: InsigniaCriteriaType, points: Double)
    suspend fun getUserPoints(userId: String): InsigniaPoints
    suspend fun addVisibleInsignia(userId: String, insigniaId: String)
    suspend fun removeVisibleInsignia(userId: String, insigniaId: String)
}