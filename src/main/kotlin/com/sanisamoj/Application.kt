package com.sanisamoj

import com.sanisamoj.config.Config
import com.sanisamoj.pluguins.configureHTTP
import com.sanisamoj.pluguins.configureRouting
import com.sanisamoj.pluguins.configureSecurity
import com.sanisamoj.pluguins.configureSerialization
import com.sanisamoj.pluguins.configureStatusPage
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureRouting()
    configureStatusPage()
    startBackgroundTasks()
}

private fun startBackgroundTasks() {
    CoroutineScope(Dispatchers.Default).launch {
        Config.initialize()
    }
}