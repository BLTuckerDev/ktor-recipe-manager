package com.bltucker.recipemanager.common.testing

import com.bltucker.recipemanager.common.models.Ingredient

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
}