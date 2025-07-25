package com.bltucker.recipemanager.recipes

import com.bltucker.recipemanager.common.models.Recipe
import com.bltucker.recipemanager.common.models.RecipeRepository
import com.bltucker.recipemanager.database.tables.Recipes
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class ExposedRecipeRepository : RecipeRepository {
    override suspend fun findAll(): List<Recipe> = newSuspendedTransaction {
        Recipes.selectAll().map { row ->
            Recipe(
                id = row[Recipes.id].toString(),
                name = row[Recipes.name],
                description = row[Recipes.description],
                prepTimeMinutes = row[Recipes.prepTimeMinutes],
                cookTimeMinutes = row[Recipes.cookTimeMinutes],
                servings = row[Recipes.servings],
                difficulty = row[Recipes.difficulty],
                createdAt = row[Recipes.createdAt].toString(),
                updatedAt = row[Recipes.updatedAt].toString()
            )
        }
    }

    override suspend fun findById(id: String): Recipe? = newSuspendedTransaction {
        Recipes.selectAll().where { Recipes.id eq UUID.fromString(id) }
            .map { row ->
                Recipe(
                    id = row[Recipes.id].toString(),
                    name = row[Recipes.name],
                    description = row[Recipes.description],
                    prepTimeMinutes = row[Recipes.prepTimeMinutes],
                    cookTimeMinutes = row[Recipes.cookTimeMinutes],
                    servings = row[Recipes.servings],
                    difficulty = row[Recipes.difficulty],
                    createdAt = row[Recipes.createdAt].toString(),
                    updatedAt = row[Recipes.updatedAt].toString()
                )
            }
            .singleOrNull()
    }

    override suspend fun create(recipe: Recipe): String = newSuspendedTransaction {
        val id = Recipes.insertAndGetId {
            it[name] = recipe.name
            it[description] = recipe.description
            it[prepTimeMinutes] = recipe.prepTimeMinutes
            it[cookTimeMinutes] = recipe.cookTimeMinutes
            it[servings] = recipe.servings
            it[difficulty] = recipe.difficulty
        }

        id.toString()
    }

    override suspend fun update(recipe: Recipe): Int = newSuspendedTransaction {
        val updated = Recipes.update({ Recipes.id eq UUID.fromString(recipe.id) }) {
            recipe.name.let { name -> it[Recipes.name] = name }
            recipe.description?.let { description -> it[Recipes.description] = description }
            recipe.prepTimeMinutes?.let { prepTime -> it[prepTimeMinutes] = prepTime }
            recipe.cookTimeMinutes?.let { cookTime -> it[cookTimeMinutes] = cookTime }
            recipe.servings?.let { servings -> it[Recipes.servings] = servings }
            recipe.difficulty?.let { difficulty -> it[Recipes.difficulty] = difficulty }
        }

        updated
    }

    override suspend fun delete(id: String): Boolean = newSuspendedTransaction {
        Recipes.deleteWhere { Recipes.id eq UUID.fromString(id) } > 0
    }
}