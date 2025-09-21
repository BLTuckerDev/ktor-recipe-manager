package com.bltucker.recipemanager.common.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*

data class UserSession(val userId: String, val email: String) : Principal

fun Application.configureSecurity() {
    val sessionSignKey = environment.config.property("jwt.secret").getString().toByteArray()
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()
    val environmentRealm = environment.config.property("jwt.realm").getString()
    
    install(Sessions) {
        cookie<UserSession>("user_session") {
            cookie.path = "/"
            transform(SessionTransportTransformerMessageAuthentication(sessionSignKey))
        }
    }

    install(Authentication) {
        jwt("auth-jwt") {

            realm = environmentRealm

            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )

            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }

        session<UserSession>("auth-session") {
            validate { session -> session }
            challenge {
                call.respondRedirect("/login")
            }
        }
    }
}