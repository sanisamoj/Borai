package com.sanisamoj.services.user

import com.sanisamoj.api.bot.MessageToSend
import com.sanisamoj.config.GlobalContext
import com.sanisamoj.config.GlobalContext.NOTIFICATION_BOT_ID
import com.sanisamoj.data.models.dataclass.*
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.enums.EventType
import com.sanisamoj.data.models.interfaces.BotRepository
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.services.media.MediaService
import com.sanisamoj.utils.analyzers.isInEnum
import com.sanisamoj.utils.generators.CharactersGenerator
import io.ktor.http.content.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bson.Document
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class UserManagerService(
    private val databaseRepository: DatabaseRepository = GlobalContext.getDatabaseRepository(),
    private val botRepository: BotRepository = GlobalContext.getBotRepository()
) {

    suspend fun updateName(userId: String, name: String): UserResponse {
        databaseRepository.updateUser(userId, OperationField(Fields.Username, value = name))
        val user: User = databaseRepository.getUserById(userId)
        return UserFactory.userResponse(user)
    }

    suspend fun updateNick(userId: String, nick: String): UserResponse {
        databaseRepository.updateUser(userId, OperationField(Fields.Nick, value = nick))
        val user: User = databaseRepository.getUserById(userId)
        return UserFactory.userResponse(user)
    }

    suspend fun updateImageProfile(multipartData: MultiPartData, userId: String): UserResponse {
        // Try saving the image
        val savedMediaResponse: List<SavedMediaResponse> = MediaService().savePublicMedia(multipartData, userId)

        // Remove the old image
        val user: User = databaseRepository.getUserById(userId)
        if (user.imageProfile != "") MediaService().deleteMedia(user.imageProfile, userId)

        // Changes the record in the new image database
        val updatedUser: User = databaseRepository.updateUser(userId, OperationField(Fields.ImageProfile, savedMediaResponse[0].filename))
        return UserFactory.userResponse(updatedUser)
    }

    suspend fun updateBio(userId: String, newBio: String) {
        databaseRepository.updateUser(userId, OperationField(Fields.Bio, newBio))
    }

    suspend fun updatePhoneProcess(userId: String, newPhone: String) {
        val validationCode: Int = CharactersGenerator.codeValidationGenerate()
        generateValidationCodeWithCustomCode(userId, validationCode)
        sendValidationCodeMessageByBot(newPhone, validationCode)
    }

    suspend fun validateValidationCodeToUpdatePhone(userId: String, newPhone: String, validationCode: Int): UserResponse {
        isCorrectCode(userId, validationCode)

        databaseRepository.updateUser(userId, OperationField(Fields.Phone, newPhone))
        val updatedUser: User = databaseRepository.getUserById(userId)
        return UserFactory.userResponse(updatedUser)
    }

    suspend fun updateAddress(userId: String, address: Address) {
        databaseRepository.updateUser(userId, OperationField(Fields.Address, address))
    }

    private suspend fun sendValidationCodeMessageByBot(phone: String, validationCode: Int) {
        val messageToSend1 = MessageToSend(phone, GlobalContext.globalWarnings.thisYourValidationCode)
        val messageToSend2 = MessageToSend(phone, validationCode.toString())
        botRepository.sendMessage(NOTIFICATION_BOT_ID, messageToSend1)
        botRepository.sendMessage(NOTIFICATION_BOT_ID, messageToSend2)
    }

    private suspend fun generateValidationCodeWithCustomCode(userId: String, validationCode: Int) {
        databaseRepository.updateUser(userId, OperationField(Fields.ValidationCode, validationCode))
        deleteValidationCodeInSpecificTime(userId)
    }

    private fun deleteValidationCodeInSpecificTime(userId: String, timeInMinutes: Long = 5) {
        val executorService = Executors.newSingleThreadScheduledExecutor()
        executorService.schedule({
            CoroutineScope(Dispatchers.IO).launch {
                val update = OperationField(Fields.ValidationCode, -1)
                databaseRepository.updateUser(userId, update)
            }
        }, timeInMinutes, TimeUnit.MINUTES)
    }

    private suspend fun isCorrectCode(userId: String, validationCode: Int) {
        val user: User = databaseRepository.getUserById(userId)
        if (user.validationCode == validationCode) {
            return
        } else if (user.validationCode == -1) {
            throw CustomException(Errors.ExpiredValidationCode)
        } else {
            throw CustomException(Errors.InvalidValidationCode)
        }
    }

    suspend fun getAllMediaStorage(userId: String): List<MediaStorage> {
        val user: User = databaseRepository.getUserById(userId)
        val mediaStorageList: MutableList<MediaStorage> = mutableListOf()
        user.mediaStorage.forEach { mediaStorageList.add(it) }
        return mediaStorageList.sortedByDescending {
            LocalDateTime.parse(it.createAt)
        }
    }

    suspend fun addMediaToTheMediaStorage(multipartData: MultiPartData, userId: String): List<MediaStorage> {
        MediaService().savePublicMedia(multipartData, userId)
        return getAllMediaStorage(userId)
    }

    suspend fun addEventPreference(userId: String, preference: String): UserResponse {
        verifyEventPreference(preference)
        val user: User = databaseRepository.getUserById(userId)

        val updatedPreferences: MutableList<String> = user.preferences.eventPreferences.toMutableList()

        if (!updatedPreferences.contains(preference)) {
            updatedPreferences.add(preference)
        } else throw CustomException(Errors.DuplicatePreference)

        val updatedUser = databaseRepository.updateUserWithQuery(
            query = Document(Fields.Id.title, user.id),
            update = Document("preferences.eventPreferences", updatedPreferences)
        )

        return UserFactory.userResponse(updatedUser)
    }

    suspend fun removeEventPreference(userId: String, preference: String): UserResponse {
        verifyEventPreference(preference)
        val user: User = databaseRepository.getUserById(userId)

        val updatedPreferences: MutableList<String> = user.preferences.eventPreferences.toMutableList()

        if (updatedPreferences.contains(preference)) {
            updatedPreferences.remove(preference)
        } else throw CustomException(Errors.UnableToComplete)

        val updatedUser = databaseRepository.updateUserWithQuery(
            query = Document(Fields.Id.title, user.id),
            update = Document("preferences.eventPreferences", updatedPreferences)
        )

        return UserFactory.userResponse(updatedUser)
    }

    private fun verifyEventPreference(preference: String) {
        if(!preference.isInEnum<EventType>()) throw CustomException(Errors.InvalidParameters)
    }

}