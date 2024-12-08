package com.sanisamoj.config

import com.sanisamoj.data.models.dataclass.GlobalWarnings
import com.sanisamoj.data.models.interfaces.*
import com.sanisamoj.utils.analyzers.ResourceLoader
import com.sanisamoj.utils.analyzers.dotEnv
import java.io.File
import java.util.concurrent.TimeUnit

object GlobalContext {
    const val VERSION: String = "0.1.16"
    private val serverContainer: ServerContainer = DefaultServerContainer()
    val globalWarnings: GlobalWarnings = ResourceLoader.convertJsonInputStreamAsObject<GlobalWarnings>("/lang/pt.json")

    val NOTIFICATION_BOT_ID: String = dotEnv("BOT_ID")

    val SELF_URL = dotEnv("SELF_URL")
    val ACTIVATE_ACCOUNT_LINK_ROUTE = "$SELF_URL/authentication/activate"
    val MEDIA_ROUTE = "$SELF_URL/media"

    val EMAIL_TOKEN_EXPIRATION: Long = TimeUnit.MINUTES.toMillis(5)
    val USER_TOKEN_EXPIRATION: Long = TimeUnit.DAYS.toMillis(90)

    const val MAX_UPLOAD_PROFILE_IMAGE: Int = 1
    const val MAX_HEADERS_SIZE: Int = 5 * 1024 * 1024 // 5MB
    val MIME_TYPE_ALLOWED: List<String> = listOf("jpeg", "png", "jpg", "gif")

    private val currentProjectDir = System.getProperty("user.dir")
    val PUBLIC_IMAGES_DIR = File(currentProjectDir, "uploads")

    fun getDatabaseRepository(): DatabaseRepository = serverContainer.databaseRepository
    fun getSessionRepository(): SessionRepository = serverContainer.sessionRepository
    fun getEventRepository(): EventRepository = serverContainer.eventRepository
    fun getBotRepository(): BotRepository = serverContainer.botRepository
    fun getMailRepository(): MailRepository = serverContainer.mailRepository
    fun getInsigniaObserver(): InsigniaRepository = serverContainer.insigniaRepository
}