package com.bltucker.recipemanager.ingredients

import kotlinx.serialization.Serializable

@Serializable
data class CreateIngredientRequest(
    val name: String,
    val category: String? = null,
    val defaultUnit: String? = null,
    val description: String? = null
)

@Serializable
data class UpdateIngredientRequest(
    val name: String? = null,
    val category: String? = null,
    val defaultUnit: String? = null,
    val description: String? = null
)