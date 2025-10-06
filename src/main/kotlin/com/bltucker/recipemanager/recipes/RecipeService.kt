package com.bltucker.recipemanager.recipes

import com.bltucker.recipemanager.common.models.Recipe
import com.bltucker.recipemanager.common.models.RecipeIngredient
import com.bltucker.recipemanager.common.repositories.RecipeRepository
import java.util.UUID
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class RecipeService(private val repository: RecipeRepository) {
    suspend fun getAllRecipes(): List<RecipeResponse> {
        val recipes = repository.findAll()
        // For each recipe, fetch its ingredients
        return recipes.map { recipe ->
            val ingredients = repository.findIngredientsByRecipeId(recipe.id)
            toRecipeResponse(recipe, ingredients)
        }
    }

    suspend fun getRecipeById(id: String): RecipeResponse? {
        val recipe = repository.findById(id) ?: return null
        val ingredients = repository.findIngredientsByRecipeId(id)
        return toRecipeResponse(recipe, ingredients)
    }


    @OptIn(ExperimentalTime::class)
    suspend fun createRecipe(request: CreateRecipeRequest): RecipeResponse {
        val now = Clock.System.now().toString()
        val recipe = Recipe(
            id = UUID.randomUUID().toString(),
            name = request.name,
            description = request.description,
            prepTimeMinutes = request.prepTimeMinutes,
            cookTimeMinutes = request.cookTimeMinutes,
            servings = request.servings,
            difficulty = request.difficulty,
            createdAt = now,
            updatedAt = now,
        )

        val insertedId = repository.create(recipe)

        request.ingredientIds?.forEach { ingredientId ->
            val recipeIngredient = RecipeIngredient(
                id = "", // Will be set by the database
                recipeId = insertedId,
                ingredientId = ingredientId,
                createdAt = "" // Will be set by the database
            )
            repository.addIngredientToRecipe(recipeIngredient)
        }

        val createdRecipe = repository.findById(insertedId)
            ?: throw IllegalStateException("Failed to retrieve created recipe")
        val ingredients = repository.findIngredientsByRecipeId(insertedId)

        return toRecipeResponse(createdRecipe, ingredients)
    }


    @OptIn(ExperimentalTime::class)
    suspend fun updateRecipe(recipe: Recipe): RecipeResponse? {
        val updatedRecipe = recipe.copy(
            updatedAt = Clock.System.now().toString()
        )

        val updatedCount = repository.update(updatedRecipe)

        return if (updatedCount > 0) {
            val fetchedRecipe = repository.findById(recipe.id)!!
            val ingredients = repository.findIngredientsByRecipeId(recipe.id)
            toRecipeResponse(fetchedRecipe, ingredients)
        } else {
            null
        }
    }



    suspend fun deleteRecipe(id: String): Boolean {
        return repository.delete(id)
    }

    // New service methods for ingredients
    suspend fun getIngredientsForRecipe(recipeId: String): List<RecipeIngredient> {
        return repository.findIngredientsByRecipeId(recipeId)
    }

    suspend fun addIngredientToRecipe(recipeIngredient: RecipeIngredient): RecipeIngredient {
        return repository.addIngredientToRecipe(recipeIngredient)
    }

    suspend fun removeIngredientFromRecipe(recipeIngredientId: String): Boolean {
        return repository.removeIngredientFromRecipe(recipeIngredientId)
    }

    private fun toRecipeResponse(recipe: Recipe, recipeIngredients: List<RecipeIngredient>) = RecipeResponse(
        id = recipe.id,
        name = recipe.name,
        description = recipe.description,
        prepTimeMinutes = recipe.prepTimeMinutes,
        cookTimeMinutes = recipe.cookTimeMinutes,
        servings = recipe.servings,
        difficulty = recipe.difficulty,
        createdAt = recipe.createdAt,
        updatedAt = recipe.updatedAt,
        // Map RecipeIngredient to Ingredient
        ingredients = recipeIngredients.mapNotNull { it.ingredient }
    )
}