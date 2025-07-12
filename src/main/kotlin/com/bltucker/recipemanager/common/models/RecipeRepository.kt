package com.bltucker.recipemanager.common.models

interface RecipeRepository {
    suspend fun findAll(): List<Recipe>
    suspend fun findById(id: String): Recipe?
    suspend fun create(recipe: Recipe): String
    suspend fun update(recipe: Recipe): Recipe?
    suspend fun delete(id: String): Boolean
}