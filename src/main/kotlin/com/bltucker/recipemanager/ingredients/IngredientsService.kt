package com.bltucker.recipemanager.ingredients

import com.bltucker.recipemanager.common.models.Ingredient
import com.bltucker.recipemanager.common.repositories.IngredientRepository
import java.util.UUID

class IngredientService(private val repository: IngredientRepository) {

    suspend fun getAllIngredients(): List<Ingredient> {
        return repository.findAll()
    }

    suspend fun getIngredientById(id: String): Ingredient? {
        return repository.findById(id)
    }

    suspend fun createIngredient(ingredient: Ingredient): Ingredient {
        // The service layer doesn't need to know about database-specific details like IDs or timestamps.
        val ingredientWithId = ingredient.copy(id = UUID.randomUUID().toString())

        val insertedId = repository.create(ingredientWithId)

        return repository.findById(insertedId)
            ?: throw IllegalStateException("Failed to retrieve created ingredient")
    }

    suspend fun updateIngredient(ingredient: Ingredient): Ingredient? {
        // The repository will handle the `updatedAt` timestamp.
        val updated = repository.update(ingredient)

        // The update method in the repository already returns the updated ingredient, so we can just return that.
        return updated
    }

    suspend fun deleteIngredient(id: String): Boolean {
        return repository.delete(id)
    }
}