package com.bltucker.recipemanager.ingredients

import com.bltucker.recipemanager.common.UserContextProvider
import com.bltucker.recipemanager.common.database.tables.Ingredients
import com.bltucker.recipemanager.common.database.tables.Ingredients.category
import com.bltucker.recipemanager.common.database.tables.Ingredients.defaultUnit
import com.bltucker.recipemanager.common.database.tables.Ingredients.description
import com.bltucker.recipemanager.common.database.tables.Ingredients.name
import com.bltucker.recipemanager.common.database.tables.RecipeIngredients
import com.bltucker.recipemanager.common.models.Ingredient
import com.bltucker.recipemanager.common.repositories.IngredientRepository
import com.bltucker.recipemanager.common.plugins.userId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.alias
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.count
import org.jetbrains.exposed.v1.core.innerJoin
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.core.lowerCase
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insertAndGetId
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update
import java.util.Locale.getDefault
import java.util.UUID
import kotlin.time.Instant

class ExposedIngredientRepository(
    private val userContextProvider: UserContextProvider,
    ) : IngredientRepository {
    override suspend fun findAll(
        category: String?,
        searchTerm: String?
    ): List<Ingredient> {
        val userId = userContextProvider.getUserId()
        return withContext(Dispatchers.IO) {
            transaction {
                var results = Ingredients.selectAll()
                    .where { Ingredients.userId eq UUID.fromString(userId) }
                    .map(ResultRow::toIngredient)

                category?.let { cat ->
                    results = results.filter { it.category == cat }
                }

                searchTerm?.let { term ->
                    results = results.filter { it.name.lowercase().contains(term.lowercase()) }
                }

                results
            }
        }
    }

    override suspend fun findById(id: String): Ingredient? {
        val userId = userContextProvider.getUserId()
        return withContext(Dispatchers.IO) {
            transaction {
                Ingredients.selectAll()
                    .where {
                        (Ingredients.id eq UUID.fromString(id)) and
                        (Ingredients.userId eq UUID.fromString(userId))
                    }
                    .map(ResultRow::toIngredient)
                    .singleOrNull()
            }
        }
    }

    override suspend fun searchByName(searchQuery: String): List<Ingredient> {
        val userId = userContextProvider.getUserId()
        return withContext(Dispatchers.IO) {
            transaction {
                Ingredients.selectAll()
                    .where {
                        (Ingredients.name.lowerCase() like "%${searchQuery.lowercase(getDefault())}%") and
                        (Ingredients.userId eq UUID.fromString(userId))
                    }
                    .map(ResultRow::toIngredient)

            }
        }
    }

    override suspend fun create(ingredient: Ingredient): String {
        val userId = userContextProvider.getUserId()
        return withContext(Dispatchers.IO) {
            transaction {
                val id = Ingredients.insertAndGetId {
                    it[name] = ingredient.name
                    it[category] = ingredient.category
                    it[description] = ingredient.description
                    it[defaultUnit] = ingredient.defaultUnit
                    it[Ingredients.userId] = UUID.fromString(userId)
                }
                id.toString()
            }
        }
    }

    override suspend fun update(ingredient: Ingredient): Ingredient? = withContext(Dispatchers.IO) {
        val userId = userContextProvider.getUserId()
        val updatedCount = transaction {
            Ingredients.update({
                (Ingredients.id eq UUID.fromString(ingredient.id)) and
                (Ingredients.userId eq UUID.fromString(userId))
            }) {
                it[name] = ingredient.name
                ingredient.category?.let { category -> it[Ingredients.category] = category }
                ingredient.description?.let { description -> it[Ingredients.description] = description }
                ingredient.defaultUnit?.let { defaultUnit -> it[Ingredients.defaultUnit] = defaultUnit }
                it[updatedAt] = CurrentDateTime
            }
        }

        // If the update was successful, fetch and return the updated entity
        if (updatedCount > 0) {
            findById(ingredient.id)
        } else {
            null
        }
    }

    override suspend fun delete(id: String): Boolean {
        val userId = userContextProvider.getUserId()
        return withContext(Dispatchers.IO) {
            transaction {
                Ingredients.deleteWhere {
                    (Ingredients.id eq UUID.fromString(id)) and
                    (Ingredients.userId eq UUID.fromString(userId))
                } > 0
            }
        }
    }

    override suspend fun findMostUsed(limit: Int): List<Ingredient> {
        val userId = userContextProvider.getUserId()
        return withContext(Dispatchers.IO) {
            transaction {
                val usageCount = RecipeIngredients.ingredientId.count()

                Ingredients
                    .innerJoin(RecipeIngredients)
                    .selectAll()
                    .where { Ingredients.userId eq UUID.fromString(userId) }
                    .groupBy(Ingredients.id)
                    .orderBy(usageCount, SortOrder.DESC)
                    .limit(limit)
                    .map { row -> row.toIngredient() }
            }
        }
    }
}


private fun ResultRow.toIngredient() = Ingredient(
    id = this[Ingredients.id].toString(),
    name = this[Ingredients.name],
    category = this[Ingredients.category],
    defaultUnit = this[Ingredients.defaultUnit],
    description = this[Ingredients.description],
    createdAt = this[Ingredients.createdAt].toString(),
    updatedAt = this[Ingredients.updatedAt].toString(),
)