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
    val environmentSecret = environment.config.property("jwt.secret").getString()
    val environmentIssuer = environment.config.property("jwt.issuer").getString()
    val environmentAudience = environment.config.property("jwt.audience").getString()

    dependencies {
        provide<UserRepository> { ExposedUserRepository() }
        provide<PasswordService> { PasswordService() }
        provide<TokenService> {
            TokenService(
                secret = environmentSecret,
                issuer = environmentIssuer,
                audience = environmentAudience,
            )
        }
        provide(UserService::class)
    }

    routing {
        webRoutes()
        apiRoutes()
    }
}



