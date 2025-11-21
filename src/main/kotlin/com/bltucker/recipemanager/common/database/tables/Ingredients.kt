package com.bltucker.recipemanager.common.database.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.timestamp

object Ingredients : UUIDTable("ingredients") {
    val userId = reference("user_id", Users.id)
    val name = varchar("name", 255).uniqueIndex()
    val category = varchar("category", 100).nullable()
    val defaultUnit = varchar("default_unit", 50).nullable()
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}

object RecipeIngredients : UUIDTable("recipe_ingredients") {
    val recipeId = uuid("recipe_id").references(Recipes.id)
    val ingredientId = uuid("ingredient_id").references(Ingredients.id)
    val quantity = decimal("quantity", 10, 3).nullable()
    val unit = varchar("unit", 50).nullable()
    val notes = text("notes").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex(recipeId, ingredientId)
    }
}