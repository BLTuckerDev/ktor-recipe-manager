package com.bltucker.recipemanager.common.repositories

import com.bltucker.recipemanager.common.models.Recipe
import com.bltucker.recipemanager.common.models.RecipeIngredient

interface RecipeRepository {
    suspend fun findAll(): List<Recipe>
    suspend fun findById(id: String): Recipe?
    suspend fun create(recipe: Recipe): String
    suspend fun update(recipe: Recipe): Int
    suspend fun delete(id: String): Boolean

    suspend fun findIngredientsByRecipeId(recipeId: String): List<RecipeIngredient>
    suspend fun addIngredientToRecipe(recipeIngredient: RecipeIngredient): RecipeIngredient
    suspend fun removeIngredientFromRecipe(recipeIngredientId: String): Boolean
}


