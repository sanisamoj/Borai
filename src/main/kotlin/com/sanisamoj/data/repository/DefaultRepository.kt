package com.sanisamoj.data.repository

import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.MediaStorage
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.MongodbOperations
import com.sanisamoj.database.mongodb.OperationField
import io.ktor.http.content.*
import org.bson.types.ObjectId
import java.io.File

class DefaultRepository: DatabaseRepository {
    override suspend fun createUser(user: User): User {
        val userId: String = MongodbOperations().register(CollectionsInDb.Users, user).toString()
        return getUserById(userId)
    }

    override suspend fun getUserById(userId: String): User {
        return MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Id, ObjectId(userId)))
            ?: throw CustomException(Errors.UserNotFound)
    }

    override suspend fun getUserByIdOrNull(userId: String): User? {
        return MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Id, ObjectId(userId)))
    }

    override suspend fun getUserByEmail(email: String): User? {
        return MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Email, email))
    }

    override suspend fun getUserByPhone(phone: String): User? {
        return MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Phone, phone))
    }

    override suspend fun updateUser(userId: String, update: OperationField): User {
        val user: User = MongodbOperations().updateAndReturnItem<User>(
            collectionName = CollectionsInDb.Users,
            filter = OperationField(Fields.Id, ObjectId(userId)),
            update = update
        ) ?: throw CustomException(Errors.UserNotFound)

        return user
    }

    override suspend fun deleteUser(userId: String) {
        MongodbOperations().deleteItem<User>(CollectionsInDb.Users, OperationField(Fields.Id, ObjectId(userId)))
    }

    override suspend fun saveMedia(
        multipartData: MultiPartData,
        maxImagesAllowed: Int
    ): List<MediaStorage> {
        TODO("Not yet implemented")
    }

    override fun getMedia(name: String): File {
        TODO("Not yet implemented")
    }

    override fun deleteMedia(file: File) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMediaFromTheBot(botId: String, filename: String) {
        TODO("Not yet implemented")
    }
}