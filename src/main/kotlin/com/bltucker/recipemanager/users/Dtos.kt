package com.bltucker.recipemanager.users

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String,
    val refreshToken: String? = null,
    val user: UserResponse
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val isVerified: Boolean,
)