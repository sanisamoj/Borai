package com.sanisamoj.data.repository

import com.mongodb.client.model.UpdateOptions
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.Insignia
import com.sanisamoj.data.models.dataclass.InsigniaPoints
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.InsigniaCriteriaType
import com.sanisamoj.data.models.interfaces.InsigniaRepository
import com.sanisamoj.database.mongodb.*
import org.bson.Document
import org.bson.types.ObjectId

object InsigniaObserver: InsigniaRepository {

    override suspend fun registerInsignia(insignia: Insignia): Insignia {
        val id: String = MongodbOperations().register(CollectionsInDb.Insignia, insignia).toString()
        return getInsigniaById(id)
    }

    override suspend fun getAllInsignias(): List<Insignia> {
        return MongodbOperations().findAll<Insignia>(CollectionsInDb.Insignia)
    }

    private suspend fun getInsigniaById(insigniaId: String): Insignia {
        return MongodbOperations().findOne<Insignia>(CollectionsInDb.Insignia, OperationField(Fields.Id, ObjectId(insigniaId)))
            ?: throw CustomException(Errors.NotFound)
    }

    override suspend fun addPoints(userId: String, insigniaCriteriaType: InsigniaCriteriaType, points: Double) {
        val query = Document(Fields.Id.title, ObjectId(userId))
        val update = Document(
            "\$inc",
            Document(insigniaCriteriaType.title, points)
        )
        val options = UpdateOptions().upsert(true)

        MongodbOperationsWithQuery().updateWithQuery<InsigniaPoints>(
            collectionName = CollectionsInDb.InsigniaPoints,
            query = query,
            update = update,
            options = options
        )

        observer(userId, insigniaCriteriaType)
    }

    override suspend fun removePoints(userId: String, insigniaCriteriaType: InsigniaCriteriaType, points: Double) {
        MongodbOperationsWithQuery().decrementValueWithQuery<InsigniaPoints>(
            collectionName = CollectionsInDb.InsigniaPoints,
            query = Document(Fields.Id.title, ObjectId(userId)),
            field = insigniaCriteriaType.title,
            decrementValue = points
        )
    }

    override suspend fun getUserPoints(userId: String): InsigniaPoints {
        return MongodbOperations().findOne(CollectionsInDb.InsigniaPoints, OperationField(Fields.Id, ObjectId(userId)))
            ?: throw CustomException(Errors.NotFound)
    }

    private suspend fun observer(userId: String, insigniaCriteriaType: InsigniaCriteriaType) {
        val user: User = getUserById(userId)
        val userPoints: InsigniaPoints = getUserPoints(userId)

        val currentPoints = when (insigniaCriteriaType) {
            InsigniaCriteriaType.Comments -> userPoints.comments ?: 0.0
            InsigniaCriteriaType.UpsComments -> userPoints.upsComments ?: 0.0
            InsigniaCriteriaType.AnswerComments -> userPoints.answerComments ?: 0.0
            InsigniaCriteriaType.Events -> userPoints.events ?: 0.0
            InsigniaCriteriaType.PresencesEvents -> userPoints.presencesEvents ?: 0.0
            InsigniaCriteriaType.CommentsEvents -> userPoints.commentsEvents ?: 0.0
            InsigniaCriteriaType.PresencesFromTheUser -> userPoints.presencesFromTheUser ?: 0.0
        }

        val query = Document()
            .append(Fields.Criteria.title, insigniaCriteriaType.title)
            .append(Fields.Quantity.title, Document("\$lte", currentPoints))

        val matchingInsignias: List<Insignia> = MongodbOperationsWithQuery()
            .findAllByFilterWithQuery<Insignia>(CollectionsInDb.Insignia, query)

        if (matchingInsignias.isNotEmpty()) {
            val updatedInsignias = (user.insignias ?: emptyList()) + matchingInsignias

            val update = Document(
                "\$set", Document("insignias", updatedInsignias.distinctBy { it.id })
            )

            MongodbOperationsWithQuery().updateWithQuery<User>(
                collectionName = CollectionsInDb.Users,
                query = Document(Fields.Id.title, ObjectId(userId)),
                update = update,
                options = UpdateOptions()
            )
        }
    }

    private suspend fun getUserById(userId: String): User {
        return MongodbOperationsWithQuery().findOneWithQuery<User>(
            CollectionsInDb.Users,
            Document(Fields.Id.title, ObjectId(userId))
        ) ?: throw CustomException(Errors.NotFound)
    }

    override suspend fun addVisibleInsignia(userId: String, insigniaId: String) {
        val user = getUserById(userId)

        val insigniaToAdd: Insignia = user.insignias?.find { it.id == ObjectId(insigniaId) }
            ?: throw CustomException(Errors.InsigniaNotFoundInUserList)

        // Checks if the limit of 7 badges has already been reached
        val visibleInsignias = user.visibleInsignias ?: emptyList()
        if (visibleInsignias.size >= 7) throw CustomException(Errors.TheLimiteVisibleInsigniaReached)

        val updatedVisibleInsignias: List<Insignia> = visibleInsignias + insigniaToAdd

        val update = Document("\$set", Document(Fields.VisibleInsignias.title, updatedVisibleInsignias))

        MongodbOperationsWithQuery().updateWithQuery<User>(
            collectionName = CollectionsInDb.Users,
            query = Document(Fields.Id.title, ObjectId(userId)),
            update = update,
            options = UpdateOptions()
        )

    }

    override suspend fun removeVisibleInsignia(userId: String, insigniaId: String) {
        val user = getUserById(userId)

        val visibleInsignias = user.visibleInsignias ?: emptyList()
        if (visibleInsignias.none { it.id == ObjectId(insigniaId) }) throw CustomException(Errors.NotFound)

        val updatedVisibleInsignias: List<Insignia> = visibleInsignias.filter { it.id != ObjectId(insigniaId) }

        val update = Document("\$set", Document("visibleInsignias", updatedVisibleInsignias))

        MongodbOperationsWithQuery().updateWithQuery<User>(
            collectionName = CollectionsInDb.Users,
            query = Document(Fields.Id.title, ObjectId(userId)),
            update = update,
            options = UpdateOptions()
        )
    }

}