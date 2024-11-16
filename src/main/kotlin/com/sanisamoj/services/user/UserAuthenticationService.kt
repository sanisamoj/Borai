package com.sanisamoj.services.user

import com.sanisamoj.config.GlobalContext
import com.sanisamoj.data.models.interfaces.DatabaseRepository
import com.sanisamoj.data.models.interfaces.MailRepository
import com.sanisamoj.data.models.interfaces.SessionRepository

class UserAuthenticationService(
    private val databaseRepository: DatabaseRepository = GlobalContext.getDatabaseRepository(),
    private val sessionRepository: SessionRepository = GlobalContext.getSessionRepository(),
    private val mailRepository: MailRepository = GlobalContext.getMailRepository()
) {



}