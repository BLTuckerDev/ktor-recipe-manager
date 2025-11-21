package com.bltucker.recipemanager.users

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

class TokenService(
    private val secret: String,
    private val issuer: String,
    private val audience: String
) {

    fun generateToken(user: User): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("email", user.email)
            .withClaim("userId", user.id)
            .withExpiresAt(Date(System.currentTimeMillis() + 3_600_000))
            .sign(Algorithm.HMAC256(secret))
    }

    fun generateRefreshToken(): String {
        return SecureTokenGenerator.generateToken()
    }

    private object SecureTokenGenerator {
        fun generateToken(): String{
            val bytes = ByteArray(32){ 0 }
            val secureRandom = java.security.SecureRandom()
            secureRandom.nextBytes(bytes)
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }
    }
}