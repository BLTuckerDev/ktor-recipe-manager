package com.bltucker.recipemanager.ingredients

import com.bltucker.recipemanager.common.models.Ingredient
import com.bltucker.recipemanager.common.repositories.IngredientRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IngredientServiceTest {
    private lateinit var repository: IngredientRepository
    private lateinit var ingredientService: IngredientService

    @BeforeEach
    fun setup() {
        repository = mockk()
        ingredientService = IngredientService(repository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("getAllIngredients")
    inner class GetAllIngredients {

        @Test
        fun `should return all ingredients`() = runTest {
            val ingredient1 = Ingredient(
                id = "ingredient-1",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val ingredient2 = Ingredient(
                id = "ingredient-2",
                name = "Basil",
                category = "Herb",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { repository.findAll() } returns listOf(ingredient1, ingredient2)

            val result = ingredientService.getAllIngredients()

            assertEquals(2, result.size)
            assertEquals("Tomato", result[0].name)
            assertEquals("Basil", result[1].name)

            coVerify { repository.findAll() }
        }

        @Test
        fun `should return empty list when no ingredients exist`() = runTest {
            coEvery { repository.findAll() } returns emptyList()

            val result = ingredientService.getAllIngredients()

            assertTrue(result.isEmpty())
            coVerify { repository.findAll() }
        }
    }

    @Nested
    @DisplayName("getIngredientById")
    inner class GetIngredientById {

        @Test
        fun `should return ingredient when found`() = runTest {
            val ingredientId = "ingredient-1"
            val ingredient = Ingredient(
                id = ingredientId,
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { repository.findById(ingredientId) } returns ingredient

            val result = ingredientService.getIngredientById(ingredientId)

            assertNotNull(result)
            assertEquals("Tomato", result.name)
            assertEquals("Vegetable", result.category)

            coVerify { repository.findById(ingredientId) }
        }

        @Test
        fun `should return null when ingredient not found`() = runTest {
            val ingredientId = "nonexistent-id"

            coEvery { repository.findById(ingredientId) } returns null

            val result = ingredientService.getIngredientById(ingredientId)

            assertNull(result)
            coVerify { repository.findById(ingredientId) }
        }
    }

    @Nested
    @DisplayName("createIngredient")
    inner class CreateIngredient {

        @Test
        fun `should create ingredient successfully`() = runTest {
            val ingredient = Ingredient(
                id = "",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "",
                updatedAt = ""
            )
            val insertedId = "ingredient-id"
            val createdIngredient = Ingredient(
                id = insertedId,
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { repository.create(any()) } returns insertedId
            coEvery { repository.findById(insertedId) } returns createdIngredient

            val result = ingredientService.createIngredient(ingredient)

            assertEquals("Tomato", result.name)
            assertEquals("Vegetable", result.category)
            assertEquals(insertedId, result.id)

            coVerify { repository.create(any()) }
            coVerify { repository.findById(insertedId) }
        }

        @Test
        fun `should throw exception when ingredient creation fails`() = runTest {
            val ingredient = Ingredient(
                id = "",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "",
                updatedAt = ""
            )
            val insertedId = "ingredient-id"

            coEvery { repository.create(any()) } returns insertedId
            coEvery { repository.findById(insertedId) } returns null

            assertThrows<IllegalStateException> {
                ingredientService.createIngredient(ingredient)
            }
        }

        @Test
        fun `should generate UUID for new ingredient`() = runTest {
            val ingredient = Ingredient(
                id = "",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "",
                updatedAt = ""
            )
            val insertedId = "generated-uuid"
            val createdIngredient = Ingredient(
                id = insertedId,
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { repository.create(match { it.id.isNotEmpty() }) } returns insertedId
            coEvery { repository.findById(insertedId) } returns createdIngredient

            val result = ingredientService.createIngredient(ingredient)

            assertNotNull(result)
            coVerify { repository.create(match { it.id.isNotEmpty() }) }
        }
    }

    @Nested
    @DisplayName("updateIngredient")
    inner class UpdateIngredient {

        @Test
        fun `should update ingredient successfully`() = runTest {
            val ingredient = Ingredient(
                id = "ingredient-1",
                name = "Cherry Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val updatedIngredient = Ingredient(
                id = "ingredient-1",
                name = "Cherry Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-02T00:00:00Z"
            )

            coEvery { repository.update(ingredient) } returns updatedIngredient

            val result = ingredientService.updateIngredient(ingredient)

            assertNotNull(result)
            assertEquals("Cherry Tomato", result.name)
            assertEquals("ingredient-1", result.id)

            coVerify { repository.update(ingredient) }
        }

        @Test
        fun `should return null when ingredient not found`() = runTest {
            val ingredient = Ingredient(
                id = "nonexistent-id",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { repository.update(ingredient) } returns null

            val result = ingredientService.updateIngredient(ingredient)

            assertNull(result)
            coVerify { repository.update(ingredient) }
        }
    }

    @Nested
    @DisplayName("deleteIngredient")
    inner class DeleteIngredient {

        @Test
        fun `should delete ingredient successfully`() = runTest {
            val ingredientId = "ingredient-1"

            coEvery { repository.delete(ingredientId) } returns true

            val result = ingredientService.deleteIngredient(ingredientId)

            assertTrue(result)
            coVerify { repository.delete(ingredientId) }
        }

        @Test
        fun `should return false when ingredient not found`() = runTest {
            val ingredientId = "nonexistent-id"

            coEvery { repository.delete(ingredientId) } returns false

            val result = ingredientService.deleteIngredient(ingredientId)

            assertEquals(false, result)
            coVerify { repository.delete(ingredientId) }
        }
    }
}
