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
                onSuccess = { (token, refreshToken) ->
                    val user = userService.getUserByEmail(request.email)!!
                    val loginResponse = LoginResponse(
                        token = token,
                        refreshToken = refreshToken,
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

        post("/refresh") {
            val userService = call.application.dependencies.resolve<UserService>()
            val request = call.receive<RefreshTokenRequest>()

            val refreshResult = userService.refreshAccessToken(request.refreshToken)
            refreshResult.fold(
                onSuccess = { (newToken, newRefreshToken) ->
                    // We need the user to populate the response.
                    // Since refreshAccessToken returns the token pair, we might need to fetch the user again or return it from the service.
                    // However, looking at the service implementation, we have the user there.
                    // Let's just decode the token to get the userId or fetch it.
                    // Actually, `refreshAccessToken` in `UserService` returns `Result<Pair<String, String>>`.
                    // We don't have the user ID easily here without decoding the token or changing the service signature.
                    // BUT, the new access token contains the user ID.
                    // Let's just fetch the user from the new access token claims? No, that's circular.
                    // Let's update `UserService.refreshAccessToken` to return Triple<String, String, User> or similar?
                    // OR, just return the tokens and let the client fetch user details if needed?
                    // The plan said "Response: LoginResponse". LoginResponse requires UserResponse.
                    
                    // I'll update the `UserService` to return the User as well, OR I'll fetch the user here.
                    // Fetching here is tricky because we don't know the user ID.
                    
                    // Wait, `refreshAccessToken` in `UserService` finds the user to generate the token.
                    // I should probably update `UserService` to return the User too.
                    // But I already wrote `UserService`.
                    
                    // Let's decode the NEW access token to get the user ID. It's a JWT.
                    val jwt = com.auth0.jwt.JWT.decode(newToken)
                    val userId = jwt.getClaim("userId").asString()
                    val user = userService.getUserById(userId)!!
                    
                    val loginResponse = LoginResponse(
                        token = newToken,
                        refreshToken = newRefreshToken,
                        user = UserResponse(
                            id = user.id,
                            email = user.email,
                            isVerified = user.isVerified,
                        )
                    )
                    call.respond(loginResponse)
                },
                onFailure = { error ->
                    call.respond(HttpStatusCode.Unauthorized, mapOf("error" to (error.message ?: "Invalid refresh token")))
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