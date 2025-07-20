package com.bltucker.recipemanager.common.database.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object Ingredients : UUIDTable("ingredients") {
    val name = varchar("name", 255).uniqueIndex()
    val category = varchar("category", 100).nullable()
    val defaultUnit = varchar("default_unit", 50).nullable()
    val description = text("description").nullable()
    val nutritionPer100g = text("nutrition_per_100g").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val updatedAt = datetime("updated_at").defaultExpression(CurrentDateTime)
}

object RecipeIngredients : UUIDTable("recipe_ingredients") {
    val recipeId = uuid("recipe_id").references(Recipes.id)
    val ingredientId = uuid("ingredient_id").references(Ingredients.id)
    val quantity = decimal("quantity", 10, 3).nullable()
    val unit = varchar("unit", 50).nullable()
    val notes = text("notes").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    init {
        uniqueIndex(recipeId, ingredientId)
    }
}