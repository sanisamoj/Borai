package com.sanisamoj.services.insignia

import com.sanisamoj.config.GlobalContextTest
import com.sanisamoj.data.models.dataclass.CreateInsigniaRequest
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.Followers
import com.sanisamoj.data.models.dataclass.InsigniaResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.InsigniaCriteriaType
import com.sanisamoj.data.models.interfaces.InsigniaRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.utils.eraseAllDataInMongodb
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class InsigniaHandlerTest {
    private val insigniaRepository: InsigniaRepository = GlobalContextTest.getInsigniaObserver()

    @AfterTest
    fun eraseAllData() {
        runBlocking {
            eraseAllDataInMongodb<Followers>(CollectionsInDb.Insignia)
        }
    }

    @Test
    fun `createInsignia should create an insignia successfully`() = testApplication {
        val insigniaHandler = InsigniaHandler(insigniaRepository)

        val request = CreateInsigniaRequest(
            name = "Excellence Badge",
            image = "badge_image_url",
            description = "Awarded for outstanding performance",
            criteria = InsigniaCriteriaType.Comments.name,
            quantity = 10.0
        )

        val insigniaResponse: InsigniaResponse = insigniaHandler.createInsignia(request)

        assertEquals(request.name, insigniaResponse.name)
        assertEquals(request.image, insigniaResponse.image)
        assertEquals(request.description, insigniaResponse.description)
        assertEquals(request.criteria, insigniaResponse.criteria)
        assertEquals(request.quantity, insigniaResponse.quantity)
    }

    @Test
    fun `createInsignia should throw an error for invalid criteria`() = testApplication {
        val insigniaHandler = InsigniaHandler(insigniaRepository)

        val request = CreateInsigniaRequest(
            name = "Invalid Badge",
            image = "invalid_image_url",
            description = "This badge has an invalid criteria",
            criteria = "InvalidCriteria",
            quantity = 5.0
        )

        val exception = assertFailsWith<CustomException> {
            insigniaHandler.createInsignia(request)
        }

        assertEquals(Errors.InvalidParameters, exception.error)
    }

    @Test
    fun `getAllInsignias should return all registered insignias`() = testApplication {
        val insigniaHandler = InsigniaHandler(insigniaRepository)

        // Register multiple insignias
        val insignias = listOf(
            CreateInsigniaRequest("Badge A", "image_a", "Desc A", InsigniaCriteriaType.Comments.name, 5.0),
            CreateInsigniaRequest("Badge B", "image_b", "Desc B", InsigniaCriteriaType.UpsComments.name, 10.0)
        )

        insignias.forEach {
            insigniaHandler.createInsignia(it)
        }

        val allInsignias: List<InsigniaResponse> = insigniaHandler.getAllInsignias()

        assertEquals(2, allInsignias.size)
        assertTrue(allInsignias.any { it.name == "Badge A" })
        assertTrue(allInsignias.any { it.name == "Badge B" })
    }

}