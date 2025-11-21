package com.bltucker.recipemanager.users

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

class UserService(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService,
    private val tokenService: TokenService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    suspend fun registerUser(email: String, password: String): Result<User> {
        return try {
            val existingUser = userRepository.findByEmail(email)
            if (existingUser != null) {
                Result.failure(Exception("User with email $email already exists"))
            } else {
                val hashedPassword = passwordService.hashPassword(password)
                val user = userRepository.createUser(email, hashedPassword)

                // TODO: Send verification email

                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun authenticateUser(email: String, password: String): Result<Pair<User, Pair<String, String?>>> {
        return try {
            val user = userRepository.findByEmail(email)
            if (user != null && passwordService.verifyPassword(password, user.hashedPassword)) {
                val accessToken = tokenService.generateToken(user)
                var refreshToken: String? = null

                if (user.isVerified) {
                    val rawRefreshToken = tokenService.generateRefreshToken()
                    val hashedRefreshToken = hashToken(rawRefreshToken)
                    // Refresh token expires in 30 days
                    val expiresAt = Clock.System.now().plus(30.days)
                    refreshTokenRepository.save(java.util.UUID.fromString(user.id), hashedRefreshToken, expiresAt)
                    refreshToken = rawRefreshToken
                }

                Result.success(user to (accessToken to refreshToken))
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshAccessToken(refreshToken: String): Result<Pair<String, String>> {
        return try {
            
            val hashedToken = hashToken(refreshToken)
            val tokenRecord = refreshTokenRepository.findByTokenHash(hashedToken)
                ?: return Result.failure(Exception("Invalid refresh token"))

            if (tokenRecord.expiresAt < Clock.System.now()) {
                refreshTokenRepository.deleteByTokenHash(hashedToken)
                return Result.failure(Exception("Refresh token expired"))
            }

            val user = userRepository.findById(tokenRecord.userId.toString())
                ?: return Result.failure(Exception("User not found"))

            // Rotation: Delete old, create new
            refreshTokenRepository.deleteByTokenHash(hashedToken)

            val newAccessToken = tokenService.generateToken(user)
            val newRawRefreshToken = tokenService.generateRefreshToken()
            val newHashedRefreshToken = hashToken(newRawRefreshToken)
            val expiresAt = Clock.System.now().plus(30.days)
            
            refreshTokenRepository.save(user.id.let { java.util.UUID.fromString(it) }, newHashedRefreshToken, expiresAt)

            Result.success(newAccessToken to newRawRefreshToken)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hashToken(token: String): String {
        val bytes = token.toByteArray()
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    suspend fun getUserByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    suspend fun getUserById(userId: String): User? {
        return userRepository.findById(userId)
    }

    suspend fun verifyUserEmail(userId: String): Boolean {
        return userRepository.markAsVerified(userId)
    }
}