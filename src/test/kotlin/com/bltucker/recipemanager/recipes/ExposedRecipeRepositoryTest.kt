package com.bltucker.recipemanager.recipes

import com.bltucker.recipemanager.common.testing.DatabaseTestBase
import com.bltucker.recipemanager.common.testing.TestDataFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("ExposedRecipeRepository")
class ExposedRecipeRepositoryTest : DatabaseTestBase() {

    private lateinit var repository: ExposedRecipeRepository

    override fun afterDatabaseSetup() {
        repository = ExposedRecipeRepository()
    }

    @Nested
    @DisplayName("Create operations")
    inner class CreateOperations {

        @Test
        fun `should create recipe and return generated ID`() = runTest {
            val recipe = TestDataFactory.createPastaRecipe()

            val createdId = repository.create(recipe)

            assertThat(createdId).isNotBlank()
            val found = repository.findById(createdId)
            assertNotNull(found)
            assertEquals("Spaghetti Bolognese", found.name)
            assertEquals("Classic Italian pasta dish with meat sauce", found.description)
            assertEquals(20, found.prepTimeMinutes)
            assertEquals(45, found.cookTimeMinutes)
            assertEquals(6, found.servings)
            assertEquals("Medium", found.difficulty)
        }

        @Test
        fun `should create recipe with minimal data`() = runTest {
            val recipe = TestDataFactory.createRecipe(
                name = "Minimal Recipe",
                description = null,
                prepTimeMinutes = null,
                cookTimeMinutes = null,
                servings = null,
                difficulty = null
            )

            val createdId = repository.create(recipe)
            val found = repository.findById(createdId)

            assertNotNull(found)
            assertEquals("Minimal Recipe", found.name)
            assertNull(found.description)
            assertNull(found.prepTimeMinutes)
            assertNull(found.cookTimeMinutes)
            assertNull(found.servings)
            assertNull(found.difficulty)
        }

        @Test
        fun `should create recipe with only cook time`() = runTest {
            val recipe = TestDataFactory.createRecipe(
                name = "No Prep Recipe",
                prepTimeMinutes = null,
                cookTimeMinutes = 25
            )

            val createdId = repository.create(recipe)
            val found = repository.findById(createdId)

            assertNotNull(found)
            assertEquals("No Prep Recipe", found.name)
            assertNull(found.prepTimeMinutes)
            assertEquals(25, found.cookTimeMinutes)
        }
    }

    @Nested
    @DisplayName("Read operations")
    inner class ReadOperations {

        @Test
        fun `should find recipe by ID`() = runTest {
            val recipe = TestDataFactory.createSaladRecipe()
            val createdId = repository.create(recipe)

            val found = repository.findById(createdId)

            assertNotNull(found)
            assertEquals(createdId, found.id)
            assertEquals("Caesar Salad", found.name)
            assertEquals("Easy", found.difficulty)
        }

        @Test
        fun `should return null when recipe not found`() = runTest {
            val found = repository.findById("550e8400-e29b-41d4-a716-446655440000")
            assertNull(found)
        }

        @Test
        fun `should find all recipes`() = runTest {
            repository.create(TestDataFactory.createPastaRecipe())
            repository.create(TestDataFactory.createSaladRecipe())
            repository.create(TestDataFactory.createDessertRecipe())

            val all = repository.findAll()

            assertEquals(3, all.size)
            assertThat(all.map { it.name }).containsExactlyInAnyOrder(
                "Spaghetti Bolognese",
                "Caesar Salad",
                "Chocolate Cake"
            )
        }

        @Test
        fun `should return empty list when no recipes exist`() = runTest {
            val all = repository.findAll()
            assertTrue(all.isEmpty())
        }

        @Test
        fun `should preserve timestamps when retrieving recipes`() = runTest {
            val recipe = TestDataFactory.createPastaRecipe()
            val createdId = repository.create(recipe)

            val found = repository.findById(createdId)

            assertNotNull(found)
            assertThat(found.createdAt).isNotBlank()
            assertThat(found.updatedAt).isNotBlank()
            assertEquals(found.createdAt, found.updatedAt) // Should be same initially
        }
    }

    @Nested
    @DisplayName("Update operations")
    inner class UpdateOperations {

        @Test
        fun `should update recipe successfully`() = runTest {
            val recipe = TestDataFactory.createSaladRecipe()
            val createdId = repository.create(recipe)

            val updated = recipe.copy(
                id = createdId,
                name = "Greek Salad",
                description = "Mediterranean salad with feta cheese",
                difficulty = "Medium"
            )

            val updateCount = repository.update(updated)

            assertEquals(1, updateCount)
            val found = repository.findById(createdId)
            assertNotNull(found)
            assertEquals("Greek Salad", found.name)
            assertEquals("Mediterranean salad with feta cheese", found.description)
            assertEquals("Medium", found.difficulty)
            assertEquals(10, found.prepTimeMinutes) // Unchanged fields remain
        }

        @Test
        fun `should return 0 when updating non-existent recipe`() = runTest {
            val recipe = TestDataFactory.createPastaRecipe().copy(id = "550e8400-e29b-41d4-a716-446655440000")

            val updateCount = repository.update(recipe)

            assertEquals(0, updateCount)
        }

        @Test
        fun `should update only provided fields`() = runTest {
            val recipe = TestDataFactory.createDessertRecipe()
            val createdId = repository.create(recipe)

            val updated = recipe.copy(
                id = createdId,
                name = "Vanilla Cake",
                prepTimeMinutes = 45,
                difficulty = "Medium"
            )

            val updateCount = repository.update(updated)

            assertEquals(1, updateCount)
            val found = repository.findById(createdId)
            assertNotNull(found)
            assertEquals("Vanilla Cake", found.name)
            assertEquals(45, found.prepTimeMinutes)
            assertEquals("Medium", found.difficulty)
            assertEquals(35, found.cookTimeMinutes) // Unchanged
            assertEquals(8, found.servings) // Unchanged
        }

        @Test
        fun `should handle null values in updates`() = runTest {
            val recipe = TestDataFactory.createPastaRecipe()
            val createdId = repository.create(recipe)

            val updated = recipe.copy(
                id = createdId,
                description = null,
                prepTimeMinutes = null
            )

            val updateCount = repository.update(updated)

            assertEquals(1, updateCount)
            val found = repository.findById(createdId)
            assertNotNull(found)
            assertEquals("Spaghetti Bolognese", found.name) // Unchanged
            assertEquals("Classic Italian pasta dish with meat sauce", found.description) // Repository doesn't set null values
            assertEquals(20, found.prepTimeMinutes) // Repository doesn't set null values
        }
    }

    @Nested
    @DisplayName("Delete operations")
    inner class DeleteOperations {

        @Test
        fun `should delete recipe successfully`() = runTest {
            val recipe = TestDataFactory.createDessertRecipe()
            val createdId = repository.create(recipe)

            val deleted = repository.delete(createdId)

            assertTrue(deleted)
            val found = repository.findById(createdId)
            assertNull(found)
        }

        @Test
        fun `should return false when deleting non-existent recipe`() = runTest {
            val deleted = repository.delete("550e8400-e29b-41d4-a716-446655440000")
            assertThat(deleted).isFalse()
        }

        @Test
        fun `should not affect other recipes when deleting`() = runTest {
            val recipe1 = TestDataFactory.createPastaRecipe()
            val recipe2 = TestDataFactory.createSaladRecipe()
            val id1 = repository.create(recipe1)
            val id2 = repository.create(recipe2)

            val deleted = repository.delete(id1)

            assertTrue(deleted)
            assertNull(repository.findById(id1))
            assertNotNull(repository.findById(id2))

            val remaining = repository.findAll()
            assertEquals(1, remaining.size)
            assertEquals("Caesar Salad", remaining.first().name)
        }
    }

    @Nested
    @DisplayName("Data integrity")
    inner class DataIntegrity {

        @Test
        fun `should handle recipes with same name`() = runTest {
            val recipe1 = TestDataFactory.createRecipe(name = "Pasta", difficulty = "Easy")
            val recipe2 = TestDataFactory.createRecipe(name = "Pasta", difficulty = "Hard")

            val id1 = repository.create(recipe1)
            val id2 = repository.create(recipe2)

            assertThat(id1).isNotEqualTo(id2)

            val found1 = repository.findById(id1)
            val found2 = repository.findById(id2)

            assertNotNull(found1)
            assertNotNull(found2)
            assertEquals("Easy", found1.difficulty)
            assertEquals("Hard", found2.difficulty)
        }

        @Test
        fun `should handle extreme values`() = runTest {
            val recipe = TestDataFactory.createRecipe(
                name = "A".repeat(255), // Max length
                prepTimeMinutes = 999,
                cookTimeMinutes = 999,
                servings = 100
            )

            val createdId = repository.create(recipe)
            val found = repository.findById(createdId)

            assertNotNull(found)
            assertEquals(255, found.name.length)
            assertEquals(999, found.prepTimeMinutes)
            assertEquals(999, found.cookTimeMinutes)
            assertEquals(100, found.servings)
        }
    }
}