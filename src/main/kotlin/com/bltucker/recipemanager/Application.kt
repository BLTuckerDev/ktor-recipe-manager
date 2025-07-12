package com.bltucker.recipemanager

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.html.*
import kotlinx.html.*
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import io.ktor.server.request.uri
import kotlinx.datetime.Clock

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureMonitoring()
    configureHTTP()
    configureRouting()
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.uri.startsWith("/") }
    }
}

fun Application.configureHTTP() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respondText(text = "500: $cause", status = io.ktor.http.HttpStatusCode.InternalServerError)
        }
    }
}

fun Application.configureRouting() {
    routing {
        get("/") {
            call.respondHtml {
                head {
                    title("Recipe Manager")
                    script(src = "https://cdn.tailwindcss.com") {}
                }
                body("bg-gray-100 min-h-screen") {
                    div("container mx-auto px-4 py-8") {
                        div("bg-white rounded-lg shadow-md p-6") {
                            h1("text-3xl font-bold text-gray-800 mb-4") {
                                +"🍳 Recipe Manager"
                            }
                            p("text-gray-600 mb-4") {
                                +"Welcome to your personal recipe management system!"
                            }
                            div("bg-blue-50 p-4 rounded") {
                                p("text-blue-800") {
                                    +"Built with Ktor and Kotlin"
                                }
                            }
                        }
                    }
                }
            }
        }
        
        get("/health") {
            call.respond(mapOf("status" to "OK", "timestamp" to Clock.System.now().toString()))
        }
        
        route("/api/v1") {
            get("/recipes") {
                call.respond(mapOf("message" to "Recipes API coming soon!"))
            }
        }
    }
}