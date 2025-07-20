package com.bltucker.recipemanager.common.repositories

import com.bltucker.recipemanager.common.models.RecipeIngredient

interface RecipeIngredientRepository {
    suspend fun findByRecipeId(recipeId: String): List<RecipeIngredient>
    suspend fun findByIngredientId(ingredientId: String): List<RecipeIngredient>
    suspend fun create(recipeIngredient: RecipeIngredient): String
    suspend fun update(recipeIngredient: RecipeIngredient): Int
    suspend fun delete(id: String): Boolean
    suspend fun deleteByRecipeId(recipeId: String): Int
}