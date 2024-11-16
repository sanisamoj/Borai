package com.sanisamoj.config

import com.sanisamoj.data.models.dataclass.GlobalWarnings
import com.sanisamoj.data.models.interfaces.BotRepository
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.MailRepository
import com.sanisamoj.data.models.interfaces.ServerContainer
import com.sanisamoj.data.models.interfaces.SessionRepository
import com.sanisamoj.utils.analyzers.ResourceLoader
import com.sanisamoj.utils.analyzers.dotEnv

object GlobalContext {
    const val VERSION: String = "0.1.0"
    private val serverContainer: ServerContainer = DefaultServerContainer()
    val globalWarnings: GlobalWarnings = ResourceLoader.convertJsonInputStreamAsObject<GlobalWarnings>("/lang/pt.json")

    val SELF_URL = dotEnv("SELF_URL")
    val ACTIVATE_ACCOUNT_LINK_ROUTE = "$SELF_URL/authentication/activate"
    val MEDIA_ROUTE = "$SELF_URL/media"

    const val MAX_UPLOAD_PROFILE_IMAGE: Int = 1

    fun getDatabaseRepository(): DatabaseRepository = serverContainer.databaseRepository
    fun getSessionRepository(): SessionRepository = serverContainer.sessionRepository
    fun getBotRepository(): BotRepository = serverContainer.botRepository
    fun getMailRepository(): MailRepository = serverContainer.mailRepository
}