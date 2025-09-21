package com.bltucker.recipemanager.common.repositories

import com.bltucker.recipemanager.common.models.Ingredient

interface IngredientRepository {
    suspend fun findAll(category: String? = null, searchTerm: String? = null): List<Ingredient>
    suspend fun findById(id: String): Ingredient?
    suspend fun searchByName(searchQuery: String): List<Ingredient>
    suspend fun create(ingredient: Ingredient): String
    suspend fun update(ingredient: Ingredient): Ingredient?
    suspend fun delete(id: String): Boolean
    suspend fun findMostUsed(limit: Int = 20): List<Ingredient>
}

