package com.bltucker.recipemanager

import com.bltucker.recipemanager.common.UserContextProvider
import com.bltucker.recipemanager.common.database.configureDatabase
import com.bltucker.recipemanager.common.plugins.configureHTTP
import com.bltucker.recipemanager.common.plugins.configureMonitoring
import com.bltucker.recipemanager.common.plugins.configureSecurity
import com.bltucker.recipemanager.common.plugins.configureSerialization
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

suspend fun Application.appModule() {
    dependencies{
        provide { UserContextProvider() }
    }

    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureDatabase()
    configureSecurity()
    configureSystemRoutes()

}

@OptIn(ExperimentalTime::class)
fun Application.configureSystemRoutes() {
    routing {

        get("/health") {
            call.respond(mapOf("status" to "OK", "timestamp" to Clock.System.now().toString()))
        }

    }
}