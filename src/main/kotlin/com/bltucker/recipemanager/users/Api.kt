package com.bltucker.recipemanager.users

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

internal fun Route.apiRoutes() {
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
                onSuccess = { (token, user) ->
                    val loginResponse = LoginResponse(
                        token = token,
                        user = UserResponse(
                            id = user.id,
                            email = user.email,
                            isVerified = user.isVerified,
                        )
                    )
                    call.respond(loginResponse)
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
                    )
                    call.respond(userResponse)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found"))
                }
            }
        }
    }
}