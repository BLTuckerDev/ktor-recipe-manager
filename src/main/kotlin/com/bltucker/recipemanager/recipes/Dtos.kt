package com.bltucker.recipemanager.recipes

import kotlinx.serialization.Serializable

@Serializable
data class CreateRecipeRequest(
    val name: String,
    val description: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int? = null,
    val difficulty: String? = null
)

@Serializable
data class UpdateRecipeRequest(
    val name: String? = null,
    val description: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int? = null,
    val difficulty: String? = null
)