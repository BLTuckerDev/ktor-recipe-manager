package com.bltucker.recipemanager.common.models

import kotlinx.serialization.Serializable

@Serializable
data class Ingredient(
    val id: String,
    val name: String,
    val category: String? = null,
    val defaultUnit: String? = null,
    val description: String? = null,
    val nutritionPer100g: String? = null,
    val createdAt: String,
    val updatedAt: String
)

