package com.bltucker.recipemanager.common.testing

import com.bltucker.recipemanager.common.models.Ingredient
import com.bltucker.recipemanager.common.models.Recipe

object TestDataFactory {
    
    fun createIngredient(
        id: String = "",
        name: String = "Test Ingredient",
        category: String? = "Test Category",
        description: String? = "Test description",
        defaultUnit: String? = "piece",
        createdAt: String = "",
        updatedAt: String = ""
    ) = Ingredient(
        id = id,
        name = name,
        category = category,
        description = description,
        defaultUnit = defaultUnit,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    fun createTomato() = createIngredient(
        name = "Tomato",
        category = "Vegetable",
        description = "Fresh red tomato",
        defaultUnit = "piece"
    )
    
    fun createCarrot() = createIngredient(
        name = "Carrot",
        category = "Vegetable",
        description = "Orange carrot",
        defaultUnit = "piece"
    )
    
    fun createSalt() = createIngredient(
        name = "Salt",
        category = "Spice",
        description = "Table salt",
        defaultUnit = "gram"
    )

    // Recipe factory methods
    fun createRecipe(
        id: String = "",
        name: String = "Test Recipe",
        description: String? = "Test recipe description",
        prepTimeMinutes: Int? = 15,
        cookTimeMinutes: Int? = 30,
        servings: Int? = 4,
        difficulty: String? = "Easy",
        createdAt: String = "",
        updatedAt: String = ""
    ) = Recipe(
        id = id,
        name = name,
        description = description,
        prepTimeMinutes = prepTimeMinutes,
        cookTimeMinutes = cookTimeMinutes,
        servings = servings,
        difficulty = difficulty,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun createPastaRecipe() = createRecipe(
        name = "Spaghetti Bolognese",
        description = "Classic Italian pasta dish with meat sauce",
        prepTimeMinutes = 20,
        cookTimeMinutes = 45,
        servings = 6,
        difficulty = "Medium"
    )

    fun createSaladRecipe() = createRecipe(
        name = "Caesar Salad",
        description = "Fresh romaine lettuce with Caesar dressing",
        prepTimeMinutes = 10,
        cookTimeMinutes = null,
        servings = 4,
        difficulty = "Easy"
    )

    fun createDessertRecipe() = createRecipe(
        name = "Chocolate Cake",
        description = "Rich chocolate layer cake",
        prepTimeMinutes = 30,
        cookTimeMinutes = 35,
        servings = 8,
        difficulty = "Hard"
    )
}