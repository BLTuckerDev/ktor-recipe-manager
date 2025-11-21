package com.bltucker.recipemanager.users

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenServiceTest {
    private lateinit var tokenService: TokenService
    private val secret = "test-secret-key-that-is-very-secure"
    private val issuer = "test-issuer"
    private val audience = "test-audience"

    @BeforeEach
    fun setup() {
        tokenService = TokenService(secret, issuer, audience)
    }

    @Nested
    @DisplayName("generateToken")
    inner class GenerateToken {

        @Test
        fun `should generate valid JWT token`() {
            val user = User(
                id = "user-123",
                email = "test@example.com",
                hashedPassword = "hashed",
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            val token = tokenService.generateToken(user)

            assertNotNull(token)
            assertTrue(token.isNotEmpty())
        }

        @Test
        fun `should include correct claims in token`() {
            val user = User(
                id = "user-123",
                email = "test@example.com",
                hashedPassword = "hashed",
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            val token = tokenService.generateToken(user)

            val verifier = JWT.require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build()

            val decodedJWT = verifier.verify(token)

            assertEquals(user.email, decodedJWT.getClaim("email").asString())
            assertEquals(user.id, decodedJWT.getClaim("userId").asString())
            assertEquals(audience, decodedJWT.audience[0])
            assertEquals(issuer, decodedJWT.issuer)
        }

        @Test
        fun `should set expiration time`() {
            val user = User(
                id = "user-123",
                email = "test@example.com",
                hashedPassword = "hashed",
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            val token = tokenService.generateToken(user)

            val verifier = JWT.require(Algorithm.HMAC256(secret))
                .withAudience(audience)
                .withIssuer(issuer)
                .build()

            val decodedJWT = verifier.verify(token)

            assertNotNull(decodedJWT.expiresAt)
            assertTrue(decodedJWT.expiresAt.time > System.currentTimeMillis())
        }

        @Test
        fun `should generate different tokens for different users`() {
            val user1 = User(
                id = "user-1",
                email = "user1@example.com",
                hashedPassword = "hashed",
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val user2 = User(
                id = "user-2",
                email = "user2@example.com",
                hashedPassword = "hashed",
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            val token1 = tokenService.generateToken(user1)
            val token2 = tokenService.generateToken(user2)

            assertTrue(token1 != token2)
        }
    }

    @Nested
    @DisplayName("generateRefreshToken")
    inner class GenerateRefreshToken {

        @Test
        fun `should generate refresh token`() {
            val refreshToken = tokenService.generateRefreshToken()

            assertNotNull(refreshToken)
            assertTrue(refreshToken.isNotEmpty())
        }

        @Test
        fun `should generate unique refresh tokens`() {
            val token1 = tokenService.generateRefreshToken()
            val token2 = tokenService.generateRefreshToken()

            assertTrue(token1 != token2)
        }

        @Test
        fun `should generate base64 url-encoded token`() {
            val refreshToken = tokenService.generateRefreshToken()

            // Base64 URL-encoded strings should only contain alphanumeric characters, - and _
            assertTrue(refreshToken.matches(Regex("^[A-Za-z0-9_-]+$")))
        }

        @Test
        fun `should generate token of sufficient length`() {
            val refreshToken = tokenService.generateRefreshToken()

            // 32 bytes encoded in base64 should be at least 40 characters
            assertTrue(refreshToken.length >= 40)
        }
    }
}
