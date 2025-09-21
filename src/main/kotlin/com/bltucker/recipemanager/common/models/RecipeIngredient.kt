package com.bltucker.recipemanager.common.models

import kotlinx.serialization.Serializable

@Serializable
data class RecipeIngredient(
    val id: String,
    val recipeId: String,
    val ingredientId: String,
    val ingredient: Ingredient? = null,
    val quantity: String? = null,
    val unit: String? = null,
    val notes: String? = null,
    val createdAt: String
)