package com.bltucker.recipemanager.recipes

import com.bltucker.recipemanager.common.models.Recipe
import com.bltucker.recipemanager.common.models.RecipeRepository
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class RecipeService(private val repository: RecipeRepository) {
    suspend fun getAllRecipes(): List<Recipe> {
        return repository.findAll()
    }

    suspend fun getRecipeById(id: String): Recipe? {
        return repository.findById(id)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun createRecipe(recipe: Recipe): Recipe {
        val recipeWithMetadata = recipe.copy(
            id = UUID.randomUUID().toString(),
            createdAt = Clock.System.now().toString(),
            updatedAt = Clock.System.now().toString()
        )

        return repository.create(recipeWithMetadata)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun updateRecipe(recipe: Recipe): Recipe? {
        val updatedRecipe = recipe.copy(
            updatedAt = Clock.System.now().toString()
        )

        return repository.update(updatedRecipe)
    }

    suspend fun deleteRecipe(id: String): Boolean {
        return repository.delete(id)
    }
}