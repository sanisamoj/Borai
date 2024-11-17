package com.sanisamoj.data.repository

import com.sanisamoj.config.GlobalContext.MIME_TYPE_ALLOWED
import com.sanisamoj.config.GlobalContext.PUBLIC_IMAGES_DIR
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.MediaStorage
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.Infos
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.CollectionsInDb
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.MongodbOperations
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.utils.generators.CharactersGenerator
import io.ktor.http.content.*
import org.bson.types.ObjectId
import java.io.File

class DefaultRepository: DatabaseRepository {
    override suspend fun createUser(user: User): User {
        val userId: String = MongodbOperations().register(CollectionsInDb.Users, user).toString()
        return getUserById(userId)
    }

    override suspend fun getUserByNick(nick: String): User? {
        return MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Nick, nick))
    }

    override suspend fun getUserById(userId: String): User {
        return MongodbOperations().findOne<User>(CollectionsInDb.Users, OperationField(Fields.Id, ObjectId(userId)))
            ?: throw CustomException(Errors.UserNotFound)
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

    override suspend fun saveMedia(multipartData: MultiPartData, maxImagesAllowed: Int): List<MediaStorage> {
        val pathToPublicImages = PUBLIC_IMAGES_DIR
        val mediaStorageList: List<MediaStorage> = saveAndReturnMediaStorageList(multipartData, pathToPublicImages, maxImagesAllowed)
        return mediaStorageList
    }

    private suspend fun saveAndReturnMediaStorageList(multipartData: MultiPartData, path: File, maxImagesAllowed: Int): List<MediaStorage> {

        val mediaStorageList: MutableList<MediaStorage> = mutableListOf()
        val imagePathOfSavedImages: MutableList<File> = mutableListOf()

        var imageCount = 0

        multipartData.forEachPart { part ->
            when (part) {

                is PartData.FileItem -> {
                    if (imageCount >= maxImagesAllowed) {
                        imagePathOfSavedImages.forEach {
                            deleteMedia(it)
                        }
                        throw CustomException(
                            error = Errors.LimitOnTheNumberOfImageReached,
                            additionalInfo = "${Infos.LimitOnThePossibleQuantityForShippingIs.description} ${maxImagesAllowed}!"
                        )
                    }

                    val mimeType: String = getType(part.originalFileName!!)
                    if (!MIME_TYPE_ALLOWED.contains(mimeType)) {
                        imagePathOfSavedImages.forEach {
                            deleteMedia(it)
                        }
                        throw CustomException(Errors.UnsupportedMediaType)
                    }

                    val fileBytes: ByteArray = part.streamProvider().readBytes()
                    val filename = "${CharactersGenerator.generateWithNoSymbols()}-${part.originalFileName}"
                    val file = File(path, filename)
                    file.writeBytes(fileBytes)

                    mediaStorageList.add(
                        MediaStorage(
                            filename = filename,
                            filesize = fileBytes.size,
                            code = null
                        )
                    )

                    imagePathOfSavedImages.add(file)
                    imageCount++
                }

                else -> {}
            }

            part.dispose()
        }

        return mediaStorageList
    }

    private fun getType(filename: String): String {
        val extension = filename.substringAfterLast('.', "")
        return extension
    }

    override fun getMedia(name: String): File {
        val file = File("$PUBLIC_IMAGES_DIR", name)
        if(!file.exists()) throw CustomException(Errors.MediaNotExist)
        else return file
    }

    override fun deleteMedia(file: File) {
        getMedia(file.name)
        if(file.exists()) file.delete()
    }
}