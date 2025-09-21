package com.bltucker.recipemanager.recipes

import com.bltucker.recipemanager.common.models.Ingredient
import kotlinx.serialization.Serializable

@Serializable
data class CreateRecipeRequest(
    val name: String,
    val description: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int? = null,
    val difficulty: String? = null,
    val ingredientIds: List<String>? = null
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

@Serializable
data class AddIngredientRequest(
    val ingredientId: String,
    val quantity: String? = null,
    val unit: String? = null,
    val notes: String? = null
)

@Serializable
data class RecipeResponse(
    val id: String,
    val name: String,
    val description: String? = null,
    val prepTimeMinutes: Int? = null,
    val cookTimeMinutes: Int? = null,
    val servings: Int? = null,
    val difficulty: String? = null,
    val createdAt: String,
    val updatedAt: String,
    val ingredients: List<Ingredient>
)