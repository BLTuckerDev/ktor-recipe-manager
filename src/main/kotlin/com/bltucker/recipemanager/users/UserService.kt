package com.bltucker.recipemanager.users

class UserService(
    private val userRepository: UserRepository,
    private val passwordService: PasswordService,
    private val tokenService: TokenService
) {

    suspend fun registerUser(email: String, password: String): Result<User> {
        return try {
            val existingUser = userRepository.findByEmail(email)
            if (existingUser != null) {
                Result.failure(Exception("User with email $email already exists"))
            } else {
                val hashedPassword = passwordService.hashPassword(password)
                val user = userRepository.createUser(email, hashedPassword)
                Result.success(user)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun authenticateUser(email: String, password: String): Result<String> {
        return try {
            val user = userRepository.findByEmail(email)
            if (user != null && passwordService.verifyPassword(password, user.hashedPassword)) {
                val token = tokenService.generateToken(user)
                Result.success(token)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): User? {
        return userRepository.findById(userId)
    }

    suspend fun verifyUserEmail(userId: String): Boolean {
        return userRepository.markAsVerified(userId)
    }
}