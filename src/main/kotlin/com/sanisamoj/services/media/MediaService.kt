package com.sanisamoj.services.media

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.config.GlobalContext.MAX_UPLOAD_PROFILE_IMAGE
import com.sanisamoj.config.GlobalContext.MEDIA_ROUTE
import com.sanisamoj.data.models.dataclass.MediaStorage
import com.sanisamoj.data.models.dataclass.SavedMediaResponse
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.OperationField
import io.ktor.http.content.*
import java.io.File

class MediaService(
    private val databaseRepository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {
    fun getMedia(mediaName: String): File {
        return databaseRepository.getMedia(mediaName)
    }

    suspend fun savePublicMedia(multipartData: MultiPartData, userId: String, maxImagesAllowed: Int = MAX_UPLOAD_PROFILE_IMAGE): List<SavedMediaResponse> {
        val mediaStorageSavedList: List<MediaStorage> = databaseRepository.saveMedia(multipartData, maxImagesAllowed)
        val saveMediaResponseList: MutableList<SavedMediaResponse> = mutableListOf()

        mediaStorageSavedList.forEach {
            saveMediaResponseList.add(SavedMediaResponse(it.filename, "$MEDIA_ROUTE?media=$it"))
        }

        val user: User = databaseRepository.getUserById(userId)
        val mediaStorage: List<MediaStorage> = user.mediaStorage + mediaStorageSavedList
        databaseRepository.updateUser(userId, OperationField(Fields.MediaStorage, mediaStorage))

        return saveMediaResponseList
    }

    suspend fun deleteMedia(mediaName: String, userId: String) {
        val imageFile: File = databaseRepository.getMedia(mediaName)
        databaseRepository.deleteMedia(imageFile)

        val user: User = databaseRepository.getUserById(userId)
        val mediaStorage: MediaStorage? = user.mediaStorage.find { it.filename == mediaName }

        if(mediaStorage != null) {
            val updatedMediaStorage = user.mediaStorage.filter { it.filename != mediaStorage.filename }
            databaseRepository.updateUser(userId, OperationField(Fields.MediaStorage, updatedMediaStorage))
        }
    }
}