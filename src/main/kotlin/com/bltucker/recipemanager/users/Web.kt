package com.bltucker.recipemanager.users

import com.bltucker.recipemanager.common.plugins.UserSession
import io.ktor.http.HttpStatusCode
import io.ktor.server.html.respondHtml
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.sessions.clear
import io.ktor.server.sessions.sessions
import io.ktor.server.sessions.set
import kotlinx.html.ButtonType
import kotlinx.html.FormMethod
import kotlinx.html.InputType
import kotlinx.html.a
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.form
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.input
import kotlinx.html.label
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.title

internal fun Route.webRoutes() {
    get("/login") {
        call.respondHtml {
            head {
                title("Login - Recipe Manager")
                script(src = "https://cdn.tailwindcss.com") {}
            }
            body("bg-gray-100 min-h-screen flex items-center justify-center") {
                div("bg-white p-8 rounded-lg shadow-md w-96") {
                    h1("text-2xl font-bold mb-6 text-center") { +"Login" }
                    form(action = "/login", method = FormMethod.post, classes = "space-y-4") {
                        div {
                            label("block text-sm font-medium text-gray-700") {
                                htmlFor = "email"
                                +"Email"
                            }
                            input(type = InputType.email, name = "email", classes = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500") {
                                required = true
                            }
                        }
                        div {
                            label("block text-sm font-medium text-gray-700") {
                                htmlFor = "password"
                                +"Password"
                            }
                            input(type = InputType.password, name = "password", classes = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500") {
                                required = true
                            }
                        }
                        button(type = ButtonType.submit, classes = "w-full bg-indigo-600 text-white py-2 px-4 rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2") {
                            +"Sign In"
                        }
                    }
                    p("mt-4 text-center") {
                        +"Don't have an account? "
                        a(href = "/register", classes = "text-indigo-600 hover:text-indigo-500") {
                            +"Register here"
                        }
                    }
                }
            }
        }
    }

    post("/login") {
        val userService = call.application.dependencies.resolve<UserService>()
        val params = call.receiveParameters()
        val email = params["email"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing email")
        val password = params["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")

        val authResult = userService.authenticateUser(email, password)
        authResult.fold(
            onSuccess = { (user, tokenPair) ->
                val (accessToken, refreshToken) = tokenPair
                call.sessions.set(UserSession(userId = user.id, email = user.email))
                call.respondRedirect("/")
            },
            onFailure = { error ->
                call.respond(HttpStatusCode.Unauthorized, error.message ?: "Authentication failed")
            }
        )
    }

    post("/logout") {
        call.sessions.clear<UserSession>() // Trashes the cookie
        call.respondRedirect("/login")     // Sends browser to login page
    }

    get("/register") {
        call.respondHtml {
            head {
                title("Register - Recipe Manager")
                script(src = "https://cdn.tailwindcss.com") {}
            }
            body("bg-gray-100 min-h-screen flex items-center justify-center") {
                div("bg-white p-8 rounded-lg shadow-md w-96") {
                    h1("text-2xl font-bold mb-6 text-center") { +"Create Account" }
                    form(action = "/register", method = FormMethod.post, classes = "space-y-4") {
                        div {
                            label("block text-sm font-medium text-gray-700") {
                                htmlFor = "email"
                                +"Email"
                            }
                            input(type = InputType.email, name = "email", classes = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500") {
                                required = true
                            }
                        }
                        div {
                            label("block text-sm font-medium text-gray-700") {
                                htmlFor = "password"
                                +"Password"
                            }
                            input(type = InputType.password, name = "password", classes = "mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500") {
                                required = true
                            }
                        }
                        button(type = ButtonType.submit, classes = "w-full bg-indigo-600 text-white py-2 px-4 rounded-md hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2") {
                            +"Create Account"
                        }
                    }
                    p("mt-4 text-center") {
                        +"Already have an account? "
                        a(href = "/login", classes = "text-indigo-600 hover:text-indigo-500") {
                            +"Sign in here"
                        }
                    }
                }
            }
        }
    }

    post("/register") {
        val userService = call.application.dependencies.resolve<UserService>()
        val params = call.receiveParameters()
        val email = params["email"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing email")
        val password = params["password"] ?: return@post call.respond(HttpStatusCode.BadRequest, "Missing password")

        val registerResult = userService.registerUser(email, password)
        registerResult.fold(
            onSuccess = { user ->
                call.sessions.set(UserSession(userId = user.id, email = user.email))
                call.respondRedirect("/")
            },
            onFailure = { error ->
                call.respond(HttpStatusCode.BadRequest, error.message ?: "Registration failed")
            }
        )
    }

    get("/logout") {
        call.sessions.clear<UserSession>()
        call.respondRedirect("/login")
    }

    get("/verify-email/{token}") {
        call.respondHtml {
            head {
                title("Email Verification - Recipe Manager")
                script(src = "https://cdn.tailwindcss.com") {}
            }
            body("bg-gray-100 min-h-screen flex items-center justify-center") {
                div("bg-white p-8 rounded-lg shadow-md w-96 text-center") {
                    h1("text-2xl font-bold mb-4 text-green-600") { +"Email Verification" }
                    p("text-gray-600 mb-4") {
                        +"Email verification functionality will be implemented in the future."
                    }
                    a(href = "/login", classes = "text-indigo-600 hover:text-indigo-500") {
                        +"Go to Login"
                    }
                }
            }
        }
    }

}