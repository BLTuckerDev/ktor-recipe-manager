package com.bltucker.recipemanager.recipes

import com.bltucker.recipemanager.common.models.Recipe
import com.bltucker.recipemanager.common.models.RecipeRepository
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

class RecipeService(private val repository: RecipeRepository) {
    suspend fun getAllRecipes(): List<Recipe> {
        return repository.findAll()
    }

    suspend fun getRecipeById(id: String): Recipe? {
        return repository.findById(id)
    }

    @OptIn(ExperimentalTime::class)
    suspend fun createRecipe(recipe: Recipe): Recipe {
        val now = Clock.System.now().toString()
        val recipeWithMetadata = recipe.copy(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            updatedAt = now,
        )

        val insertedId = repository.create(recipeWithMetadata)

        return repository.findById(insertedId)
            ?: throw IllegalStateException("Failed to retrieve created recipe")
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