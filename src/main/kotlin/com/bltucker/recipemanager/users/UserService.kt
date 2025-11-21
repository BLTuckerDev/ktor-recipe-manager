package com.bltucker.recipemanager.users

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

    suspend fun authenticateUser(email: String, password: String): Result<Pair<String, String?>> {
        return try {
            val user = userRepository.findByEmail(email)
            if (user != null && passwordService.verifyPassword(password, user.hashedPassword)) {
                val accessToken = tokenService.generateToken(user)
                var refreshToken: String? = null

                if (user.isVerified) {
                    val rawRefreshToken = tokenService.generateRefreshToken()
                    val hashedRefreshToken = passwordService.hashPassword(rawRefreshToken)
                    // Refresh token expires in 30 days
                    val expiresAt = java.time.Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS)
                    refreshTokenRepository.save(java.util.UUID.fromString(user.id), hashedRefreshToken, expiresAt)
                    refreshToken = rawRefreshToken
                }

                Result.success(accessToken to refreshToken)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
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