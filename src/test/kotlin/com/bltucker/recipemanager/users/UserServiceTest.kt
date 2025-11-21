package com.bltucker.recipemanager.users

import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
    private lateinit var userRepository: UserRepository
    private lateinit var passwordService: PasswordService
    private lateinit var tokenService: TokenService
    private lateinit var refreshTokenRepository: RefreshTokenRepository
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        userRepository = mockk()
        passwordService = mockk()
        tokenService = mockk()
        refreshTokenRepository = mockk()
        userService = UserService(userRepository, passwordService, tokenService, refreshTokenRepository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("registerUser")
    inner class RegisterUser {

        @Test
        fun `should register new user successfully`() = runTest {
            val email = "test@example.com"
            val password = "password123"
            val hashedPassword = "hashedPassword"
            val user = User(
                id = "user-id",
                email = email,
                hashedPassword = hashedPassword,
                isVerified = false,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { userRepository.findByEmail(email) } returns null
            every { passwordService.hashPassword(password) } returns hashedPassword
            coEvery { userRepository.createUser(email, hashedPassword) } returns user

            val result = userService.registerUser(email, password)

            assertTrue(result.isSuccess)
            assertEquals(user, result.getOrNull())
            coVerify { userRepository.findByEmail(email) }
            verify { passwordService.hashPassword(password) }
            coVerify { userRepository.createUser(email, hashedPassword) }
        }

        @Test
        fun `should fail when user already exists`() = runTest {
            val email = "existing@example.com"
            val password = "password123"
            val existingUser = User(
                id = "user-id",
                email = email,
                hashedPassword = "hashedPassword",
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { userRepository.findByEmail(email) } returns existingUser

            val result = userService.registerUser(email, password)

            assertTrue(result.isFailure)
            assertEquals("User with email $email already exists", result.exceptionOrNull()?.message)
            coVerify { userRepository.findByEmail(email) }
            verify(exactly = 0) { passwordService.hashPassword(any()) }
            coVerify(exactly = 0) { userRepository.createUser(any(), any()) }
        }

        @Test
        fun `should handle repository exception`() = runTest {
            val email = "test@example.com"
            val password = "password123"

            coEvery { userRepository.findByEmail(email) } throws RuntimeException("Database error")

            val result = userService.registerUser(email, password)

            assertTrue(result.isFailure)
            assertEquals("Database error", result.exceptionOrNull()?.message)
        }
    }

    @Nested
    @DisplayName("authenticateUser")
    inner class AuthenticateUser {

        @Test
        fun `should authenticate verified user and return tokens`() = runTest {
            val email = "test@example.com"
            val password = "password123"
            val hashedPassword = "hashedPassword"
            val userId = java.util.UUID.randomUUID().toString()
            val user = User(
                id = userId,
                email = email,
                hashedPassword = hashedPassword,
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val accessToken = "access-token"
            val refreshToken = "refresh-token"

            coEvery { userRepository.findByEmail(email) } returns user
            every { passwordService.verifyPassword(password, hashedPassword) } returns true
            every { tokenService.generateToken(user) } returns accessToken
            every { tokenService.generateRefreshToken() } returns refreshToken
            coEvery { refreshTokenRepository.save(any(), any(), any()) } just Runs

            val result = userService.authenticateUser(email, password)

            assertTrue(result.isSuccess)
            val (returnedUser, tokens) = result.getOrNull()!!
            assertEquals(user, returnedUser)
            assertEquals(accessToken, tokens.first)
            assertEquals(refreshToken, tokens.second)

            coVerify { userRepository.findByEmail(email) }
            verify { passwordService.verifyPassword(password, hashedPassword) }
            verify { tokenService.generateToken(user) }
            verify { tokenService.generateRefreshToken() }
            coVerify { refreshTokenRepository.save(any(), any(), any()) }
        }

        @Test
        fun `should authenticate unverified user without refresh token`() = runTest {
            val email = "test@example.com"
            val password = "password123"
            val hashedPassword = "hashedPassword"
            val user = User(
                id = "user-id",
                email = email,
                hashedPassword = hashedPassword,
                isVerified = false,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val accessToken = "access-token"

            coEvery { userRepository.findByEmail(email) } returns user
            every { passwordService.verifyPassword(password, hashedPassword) } returns true
            every { tokenService.generateToken(user) } returns accessToken

            val result = userService.authenticateUser(email, password)

            assertTrue(result.isSuccess)
            val (returnedUser, tokens) = result.getOrNull()!!
            assertEquals(user, returnedUser)
            assertEquals(accessToken, tokens.first)
            assertEquals(null, tokens.second)

            verify(exactly = 0) { tokenService.generateRefreshToken() }
            coVerify(exactly = 0) { refreshTokenRepository.save(any(), any(), any()) }
        }

        @Test
        fun `should fail with invalid email`() = runTest {
            val email = "nonexistent@example.com"
            val password = "password123"

            coEvery { userRepository.findByEmail(email) } returns null

            val result = userService.authenticateUser(email, password)

            assertTrue(result.isFailure)
            assertEquals("Invalid email or password", result.exceptionOrNull()?.message)
        }

        @Test
        fun `should fail with invalid password`() = runTest {
            val email = "test@example.com"
            val password = "wrongpassword"
            val hashedPassword = "hashedPassword"
            val user = User(
                id = "user-id",
                email = email,
                hashedPassword = hashedPassword,
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { userRepository.findByEmail(email) } returns user
            every { passwordService.verifyPassword(password, hashedPassword) } returns false

            val result = userService.authenticateUser(email, password)

            assertTrue(result.isFailure)
            assertEquals("Invalid email or password", result.exceptionOrNull()?.message)
        }
    }

    @Nested
    @DisplayName("refreshAccessToken")
    inner class RefreshAccessToken {

        @Test
        fun `should refresh tokens successfully`() = runTest {
            val refreshToken = "refresh-token"
            val userId = java.util.UUID.randomUUID()
            val user = User(
                id = userId.toString(),
                email = "test@example.com",
                hashedPassword = "hashedPassword",
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            // Use a future expiration time to ensure token is valid
            val tokenRecord = RefreshTokenRecord(
                id = 1L,
                userId = userId,
                token = "any-hash",
                expiresAt = Clock.System.now().plus(30.days),
                createdAt = Clock.System.now()
            )
            val newAccessToken = "new-access-token"
            val newRefreshToken = "new-refresh-token"

            coEvery { refreshTokenRepository.findByTokenHash(any()) } returns tokenRecord
            coEvery { userRepository.findById(userId.toString()) } returns user
            coEvery { refreshTokenRepository.deleteByTokenHash(any()) } just Runs
            every { tokenService.generateToken(user) } returns newAccessToken
            every { tokenService.generateRefreshToken() } returns newRefreshToken
            coEvery { refreshTokenRepository.save(any(), any(), any()) } just Runs

            val result = userService.refreshAccessToken(refreshToken)

            assertTrue(result.isSuccess)
            val (returnedAccessToken, returnedRefreshToken) = result.getOrNull()!!
            assertEquals(newAccessToken, returnedAccessToken)
            assertEquals(newRefreshToken, returnedRefreshToken)
        }

        @Test
        fun `should fail with invalid refresh token`() = runTest {
            val refreshToken = "invalid-token"

            coEvery { refreshTokenRepository.findByTokenHash(any()) } returns null

            val result = userService.refreshAccessToken(refreshToken)

            assertTrue(result.isFailure)
            assertEquals("Invalid refresh token", result.exceptionOrNull()?.message)
        }

        @Test
        fun `should fail when user not found`() = runTest {
            val refreshToken = "refresh-token"
            val userId = java.util.UUID.randomUUID()
            val tokenRecord = RefreshTokenRecord(
                id = 1L,
                userId = userId,
                token = "any-hash",
                expiresAt = Clock.System.now().plus(30.days),
                createdAt = Clock.System.now()
            )

            coEvery { refreshTokenRepository.findByTokenHash(any()) } returns tokenRecord
            coEvery { userRepository.findById(userId.toString()) } returns null

            val result = userService.refreshAccessToken(refreshToken)

            assertTrue(result.isFailure)
            assertEquals("User not found", result.exceptionOrNull()?.message)
        }
    }

    @Nested
    @DisplayName("getUserByEmail")
    inner class GetUserByEmail {

        @Test
        fun `should return user when found`() = runTest {
            val email = "test@example.com"
            val user = User(
                id = "user-id",
                email = email,
                hashedPassword = "hashedPassword",
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { userRepository.findByEmail(email) } returns user

            val result = userService.getUserByEmail(email)

            assertEquals(user, result)
            coVerify { userRepository.findByEmail(email) }
        }

        @Test
        fun `should return null when user not found`() = runTest {
            val email = "nonexistent@example.com"

            coEvery { userRepository.findByEmail(email) } returns null

            val result = userService.getUserByEmail(email)

            assertEquals(null, result)
        }
    }

    @Nested
    @DisplayName("getUserById")
    inner class GetUserById {

        @Test
        fun `should return user when found`() = runTest {
            val userId = "user-id"
            val user = User(
                id = userId,
                email = "test@example.com",
                hashedPassword = "hashedPassword",
                isVerified = true,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { userRepository.findById(userId) } returns user

            val result = userService.getUserById(userId)

            assertEquals(user, result)
            coVerify { userRepository.findById(userId) }
        }

        @Test
        fun `should return null when user not found`() = runTest {
            val userId = "nonexistent-id"

            coEvery { userRepository.findById(userId) } returns null

            val result = userService.getUserById(userId)

            assertEquals(null, result)
        }
    }

    @Nested
    @DisplayName("verifyUserEmail")
    inner class VerifyUserEmail {

        @Test
        fun `should verify user email successfully`() = runTest {
            val userId = "user-id"

            coEvery { userRepository.markAsVerified(userId) } returns true

            val result = userService.verifyUserEmail(userId)

            assertTrue(result)
            coVerify { userRepository.markAsVerified(userId) }
        }

        @Test
        fun `should return false when verification fails`() = runTest {
            val userId = "nonexistent-id"

            coEvery { userRepository.markAsVerified(userId) } returns false

            val result = userService.verifyUserEmail(userId)

            assertEquals(false, result)
        }
    }
}