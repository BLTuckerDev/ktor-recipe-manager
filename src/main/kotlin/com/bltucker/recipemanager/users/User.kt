package com.bltucker.recipemanager.users

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val email: String,
    val hashedPassword: String,
    val isVerified: Boolean = false,
    val createdAt: String,
    val updatedAt: String
)