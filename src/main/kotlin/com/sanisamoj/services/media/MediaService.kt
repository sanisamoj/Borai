package com.sanisamoj.services.media

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.config.GlobalContext.MAX_UPLOAD_PROFILE_IMAGE
import com.sanisamoj.config.GlobalContext.MEDIA_ROUTE
import com.sanisamoj.data.models.dataclass.MediaStorage
import com.sanisamoj.data.models.dataclass.SavedMediaResponse
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import io.ktor.http.content.*
import java.io.File

class MediaService(
    private val databaseRepository: DatabaseRepository = GlobalContext.getDatabaseRepository()
) {
    fun getMedia(mediaName: String): File {
        return databaseRepository.getMedia(mediaName)
    }

    suspend fun savePublicMedia(multipartData: MultiPartData, maxImagesAllowed: Int = MAX_UPLOAD_PROFILE_IMAGE): List<SavedMediaResponse> {
        val mediaStorageSavedList: List<MediaStorage> = databaseRepository.saveMedia(multipartData, maxImagesAllowed)
        val saveMediaResponseList: MutableList<SavedMediaResponse> = mutableListOf()

        mediaStorageSavedList.forEach {
            saveMediaResponseList.add(SavedMediaResponse(it.filename, "$MEDIA_ROUTE?media=$it"))
        }

        return saveMediaResponseList
    }

    fun deleteMedia(mediaName: String) {
        val imageFile: File = databaseRepository.getMedia(mediaName)
        databaseRepository.deleteMedia(imageFile)
    }
}