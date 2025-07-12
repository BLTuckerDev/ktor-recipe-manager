package com.bltucker.recipemanager.common.models

import kotlinx.serialization.Serializable

@Serializable
data class Recipe(
    val id: String,
    val name: String,
    val description: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int? = null,
    val difficulty: String? = null,
    val createdAt: String,
    val updatedAt: String
)