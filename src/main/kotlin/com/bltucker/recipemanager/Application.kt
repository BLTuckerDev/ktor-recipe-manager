package com.bltucker.recipemanager

import com.bltucker.recipemanager.plugins.configureHTTP
import com.bltucker.recipemanager.plugins.configureMonitoring
import com.bltucker.recipemanager.plugins.configureSerialization
import com.bltucker.recipemanager.recipes.recipesModule
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import kotlinx.html.*
import kotlinx.datetime.Clock

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureSystemRoutes()

    recipesModule()
}





fun Application.configureSystemRoutes() {
    routing {

        get("/health") {
            call.respond(mapOf("status" to "OK", "timestamp" to Clock.System.now().toString()))
        }

    }
}