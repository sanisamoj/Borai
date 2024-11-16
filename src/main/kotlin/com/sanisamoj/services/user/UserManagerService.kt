package com.sanisamoj.services.user

import com.sanisamoj.api.bot.MessageToSend
import com.sanisamoj.config.GlobalContext
import com.sanisamoj.config.GlobalContext.NOTIFICATION_BOT_ID
import com.sanisamoj.data.models.dataclass.CustomException
import com.sanisamoj.data.models.dataclass.SavedMediaResponse
import com.sanisamoj.data.models.dataclass.User
import com.sanisamoj.data.models.dataclass.UserResponse
import com.sanisamoj.data.models.enums.Errors
import com.sanisamoj.data.models.interfaces.BotRepository
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.database.mongodb.Fields
import com.sanisamoj.database.mongodb.OperationField
import com.sanisamoj.services.media.MediaService
import com.sanisamoj.utils.generators.CharactersGenerator
import io.ktor.http.content.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

    suspend fun updateImageProfile(multipartData: MultiPartData, userId: String): UserResponse {
        // Try saving the image
        val savedMediaResponse: List<SavedMediaResponse> = MediaService().savePublicMedia(multipartData)

        // Remove the old image
        val user: User = databaseRepository.getUserById(userId)
        if (user.imageProfile != "") MediaService().deleteMedia(user.imageProfile)

        // Changes the record in the new image database
        val updatedUser: User = databaseRepository.updateUser(userId, OperationField(Fields.ImageProfile, savedMediaResponse[0].filename))
        return UserFactory.userResponse(updatedUser)
    }

    suspend fun updatePhoneProcess(userId: String, newPhone: String) {
        val validationCode: Int = CharactersGenerator.codeValidationGenerate()
        generateValidationCodeWithCustomCode(userId, validationCode)
        sendValidationCodeMessageByBot(newPhone, validationCode)
    }

    suspend fun validateValidationCodeToUpdatePhone(
        userId: String,
        newPhone: String,
        validationCode: Int
    ): UserResponse {
        isCorrectCode(userId, validationCode)

        databaseRepository.updateUser(userId, OperationField(Fields.Phone, newPhone))
        val updatedUser: User = databaseRepository.getUserById(userId)
        return UserFactory.userResponse(updatedUser)
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

}