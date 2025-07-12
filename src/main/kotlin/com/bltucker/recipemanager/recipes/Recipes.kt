package com.bltucker.recipemanager.recipes

import io.ktor.server.application.Application
import io.ktor.server.html.respondHtml
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.title


fun Application.recipesModule() {
    routing{
        webRoutes()
        apiRoutes()
    }

}

private fun Route.webRoutes() {
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
}


private fun Route.apiRoutes(){

    route("/api/v1") {
        get("/recipes") {
            call.respond(mapOf("message" to "Recipes API coming soon!"))
        }
    }
}