package com.bltucker.recipemanager.recipes


import com.bltucker.recipemanager.common.database.tables.Ingredients
import com.bltucker.recipemanager.common.database.tables.RecipeIngredients
import com.bltucker.recipemanager.common.database.tables.Recipes
import com.bltucker.recipemanager.common.models.Ingredient
import com.bltucker.recipemanager.common.models.Recipe
import com.bltucker.recipemanager.common.models.RecipeIngredient
import com.bltucker.recipemanager.common.repositories.RecipeRepository
import com.bltucker.recipemanager.common.plugins.userId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.UUID

class ExposedRecipeRepository : RecipeRepository {
    override suspend fun findAll(): List<Recipe> = withContext(Dispatchers.IO) {
        val userId = coroutineContext.userId
        transaction {
            Recipes.selectAll()
                .where { Recipes.userId eq UUID.fromString(userId) }
                .map(ResultRow::toRecipe)
        }
    }

    override suspend fun findById(id: String): Recipe? = withContext(Dispatchers.IO) {
        val userId = coroutineContext.userId
        transaction {
            Recipes.selectAll().where {
                (Recipes.id eq UUID.fromString(id)) and
                (Recipes.userId eq UUID.fromString(userId))
            }
                .map(ResultRow::toRecipe)
                .singleOrNull()
        }
    }

    override suspend fun create(recipe: Recipe): String = withContext(Dispatchers.IO) {
        val userId = coroutineContext.userId
        transaction {
            val id = Recipes.insertAndGetId {
                it[name] = recipe.name
                it[Recipes.userId] = UUID.fromString(userId)
                it[description] = recipe.description
                it[prepTimeMinutes] = recipe.prepTimeMinutes
                it[cookTimeMinutes] = recipe.cookTimeMinutes
                it[servings] = recipe.servings
                it[difficulty] = recipe.difficulty
            }

            id.toString()
        }
    }

    override suspend fun update(recipe: Recipe): Int = withContext(Dispatchers.IO) {
        val userId = coroutineContext.userId
        transaction {

            val updated = Recipes.update({
                (Recipes.id eq UUID.fromString(recipe.id)) and
                (Recipes.userId eq UUID.fromString(userId))
            }) {
                recipe.name.let { name -> it[Recipes.name] = name }
                recipe.description?.let { description -> it[Recipes.description] = description }
                recipe.prepTimeMinutes?.let { prepTime -> it[prepTimeMinutes] = prepTime }
                recipe.cookTimeMinutes?.let { cookTime -> it[cookTimeMinutes] = cookTime }
                recipe.servings?.let { servings -> it[Recipes.servings] = servings }
                recipe.difficulty?.let { difficulty -> it[Recipes.difficulty] = difficulty }
                recipe.updatedAt?.let { _ -> it[Recipes.updatedAt] = CurrentDateTime }
            }

            updated
        }

    }

    override suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        val userId = coroutineContext.userId
        transaction {
            Recipes.deleteWhere {
                (Recipes.id eq UUID.fromString(id)) and
                (Recipes.userId eq UUID.fromString(userId))
            } > 0
        }
    }

    override suspend fun findIngredientsByRecipeId(recipeId: String): List<RecipeIngredient> = withContext(Dispatchers.IO) {
        val userId = coroutineContext.userId
        transaction {
            RecipeIngredients
                .innerJoin(Ingredients, { ingredientId }, { Ingredients.id })
                .innerJoin(Recipes, { RecipeIngredients.recipeId }, { Recipes.id })
                .selectAll()
                .where {
                    (RecipeIngredients.recipeId eq UUID.fromString(recipeId)) and
                    (Recipes.userId eq UUID.fromString(userId))
                }
                .map(ResultRow::toRecipeIngredient)
        }
    }

    override suspend fun addIngredientToRecipe(recipeIngredient: RecipeIngredient): RecipeIngredient = withContext(Dispatchers.IO) {
        transaction {
            val id = RecipeIngredients.insertAndGetId {
                it[recipeId] = UUID.fromString(recipeIngredient.recipeId)
                it[ingredientId] = UUID.fromString(recipeIngredient.ingredientId)
                it[quantity] = recipeIngredient.quantity?.toBigDecimalOrNull() ?: 0.toBigDecimal()
                it[unit] = recipeIngredient.unit
                it[notes] = recipeIngredient.notes
            }
            // Return the original object with the new ID
            recipeIngredient.copy(id = id.toString())
        }
    }

    override suspend fun removeIngredientFromRecipe(recipeIngredientId: String): Boolean = withContext(Dispatchers.IO) {
        transaction {
            RecipeIngredients.deleteWhere { RecipeIngredients.id eq UUID.fromString(recipeIngredientId) } > 0
        }
    }
}

private fun ResultRow.toRecipe() = Recipe(
    id = this[Recipes.id].toString(),
    name = this[Recipes.name],
    description = this[Recipes.description],
    prepTimeMinutes = this[Recipes.prepTimeMinutes],
    cookTimeMinutes = this[Recipes.cookTimeMinutes],
    servings = this[Recipes.servings],
    difficulty = this[Recipes.difficulty],
    createdAt = this[Recipes.createdAt].toString(),
    updatedAt = this[Recipes.updatedAt].toString()
)


private fun ResultRow.toRecipeIngredient() = RecipeIngredient(
    id = this[RecipeIngredients.id].toString(),
    recipeId = this[RecipeIngredients.recipeId].toString(),
    ingredientId = this[RecipeIngredients.ingredientId].toString(),
    quantity = this[RecipeIngredients.quantity].toString(),
    unit = this[RecipeIngredients.unit],
    notes = this[RecipeIngredients.notes],
    createdAt = this[RecipeIngredients.createdAt].toString(),
    ingredient = Ingredient(
        id = this[Ingredients.id].toString(),
        name = this[Ingredients.name],
        category = this[Ingredients.category],
        defaultUnit = this[Ingredients.defaultUnit],
        description = this[Ingredients.description],
        createdAt = this[Ingredients.createdAt].toString(),
        updatedAt = this[Ingredients.updatedAt].toString()
    )
)