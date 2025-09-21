package com.bltucker.recipemanager.users

import com.bltucker.recipemanager.common.plugins.UserSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.html.*
import io.ktor.server.plugins.di.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import kotlinx.html.*

fun Application.usersModule() {
    dependencies {
        provide<UserRepository> { ExposedUserRepository() }
        provide<PasswordService> { PasswordService() }
        provide<TokenService> {
            TokenService(
                secret = environment.config.property("jwt.secret").getString(),
                issuer = environment.config.property("jwt.issuer").getString(),
                audience = environment.config.property("jwt.audience").getString()
            )
        }
        provide(UserService::class)
    }

    routing {
        webRoutes()
        apiRoutes()
    }
}

private fun Route.webRoutes() {
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
            onSuccess = { token ->
                val user = userService.getUserById(token.split(".")[1]) // This is simplified; you'd decode the JWT properly
                if (user != null) {
                    call.sessions.set(UserSession(userId = user.id, email = user.email))
                    call.respondRedirect("/")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to retrieve user")
                }
            },
            onFailure = { error ->
                call.respond(HttpStatusCode.Unauthorized, error.message ?: "Authentication failed")
            }
        )
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
}

private fun Route.apiRoutes() {
    route("/api/v1/users") {
        post("/register") {
            val userService = call.application.dependencies.resolve<UserService>()
            val request = call.receive<RegisterRequest>()

            val registerResult = userService.registerUser(request.email, request.password)
            registerResult.fold(
                onSuccess = { user ->
                    val userResponse = UserResponse(
                        id = user.id,
                        email = user.email,
                        isVerified = user.isVerified,
                        createdAt = user.createdAt
                    )
                    call.respond(HttpStatusCode.Created, userResponse)
                },
                onFailure = { error ->
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to (error.message ?: "Registration failed")))
                }
            )
        }

        post("/login") {
            val userService = call.application.dependencies.resolve<UserService>()
            val request = call.receive<LoginRequest>()

            val authResult = userService.authenticateUser(request.email, request.password)
            authResult.fold(
                onSuccess = { token ->
                    val user = userService.getUserById(request.email) // This should be fixed to get user from token
                    if (user != null) {
                        val loginResponse = LoginResponse(
                            token = token,
                            user = UserResponse(
                                id = user.id,
                                email = user.email,
                                isVerified = user.isVerified,
                                createdAt = user.createdAt
                            )
                        )
                        call.respond(loginResponse)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to retrieve user"))
                    }
                },
                onFailure = { error ->
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to (error.message ?: "Authentication failed")))
                }
            )
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
                val userService = call.application.dependencies.resolve<UserService>()

                val user = userService.getUserById(userId)
                if (user != null) {
                    val userResponse = UserResponse(
                        id = user.id,
                        email = user.email,
                        isVerified = user.isVerified,
                        createdAt = user.createdAt
                    )
                    call.respond(userResponse)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                }
            }
        }
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