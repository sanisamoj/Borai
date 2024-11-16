package com.sanisamoj.config

import com.sanisamoj.data.repository.WhatsappBotRepository
import com.sanisamoj.errors.Logger
import com.sanisamoj.database.mongodb.MongoDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object Config {
    private val backgroundScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    suspend fun initialize() {
        MongoDatabase.initialize()

        backgroundScope.launch { WhatsappBotRepository.updateToken() }
        backgroundScope.launch { Logger.updateToken() }
    }

}