package com.bltucker.recipemanager.recipes

import com.bltucker.recipemanager.common.models.Ingredient
import com.bltucker.recipemanager.common.models.Recipe
import com.bltucker.recipemanager.common.models.RecipeIngredient
import com.bltucker.recipemanager.common.repositories.RecipeRepository
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RecipeServiceTest {
    private lateinit var repository: RecipeRepository
    private lateinit var recipeService: RecipeService

    @BeforeEach
    fun setup() {
        repository = mockk()
        recipeService = RecipeService(repository)
    }

    @AfterEach
    fun teardown() {
        clearAllMocks()
    }

    @Nested
    @DisplayName("getAllRecipes")
    inner class GetAllRecipes {

        @Test
        fun `should return all recipes with ingredients`() = runTest {
            val recipe1 = Recipe(
                id = "recipe-1",
                name = "Pasta",
                description = "Delicious pasta",
                prepTimeMinutes = 10,
                cookTimeMinutes = 20,
                servings = 4,
                difficulty = "Easy",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val recipe2 = Recipe(
                id = "recipe-2",
                name = "Salad",
                description = "Fresh salad",
                prepTimeMinutes = 5,
                cookTimeMinutes = 0,
                servings = 2,
                difficulty = "Easy",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val ingredient1 = Ingredient(
                id = "ingredient-1",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val recipeIngredient1 = RecipeIngredient(
                id = "ri-1",
                recipeId = "recipe-1",
                ingredientId = "ingredient-1",
                createdAt = "2025-01-01T00:00:00Z",
                ingredient = ingredient1
            )

            coEvery { repository.findAll() } returns listOf(recipe1, recipe2)
            coEvery { repository.findIngredientsByRecipeId("recipe-1") } returns listOf(recipeIngredient1)
            coEvery { repository.findIngredientsByRecipeId("recipe-2") } returns emptyList()

            val result = recipeService.getAllRecipes()

            assertEquals(2, result.size)
            assertEquals("Pasta", result[0].name)
            assertEquals(1, result[0].ingredients.size)
            assertEquals("Tomato", result[0].ingredients[0].name)
            assertEquals("Salad", result[1].name)
            assertEquals(0, result[1].ingredients.size)

            coVerify { repository.findAll() }
            coVerify { repository.findIngredientsByRecipeId("recipe-1") }
            coVerify { repository.findIngredientsByRecipeId("recipe-2") }
        }

        @Test
        fun `should return empty list when no recipes exist`() = runTest {
            coEvery { repository.findAll() } returns emptyList()

            val result = recipeService.getAllRecipes()

            assertTrue(result.isEmpty())
            coVerify { repository.findAll() }
        }
    }

    @Nested
    @DisplayName("getRecipeById")
    inner class GetRecipeById {

        @Test
        fun `should return recipe with ingredients when found`() = runTest {
            val recipeId = "recipe-1"
            val recipe = Recipe(
                id = recipeId,
                name = "Pasta",
                description = "Delicious pasta",
                prepTimeMinutes = 10,
                cookTimeMinutes = 20,
                servings = 4,
                difficulty = "Easy",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val ingredient = Ingredient(
                id = "ingredient-1",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val recipeIngredient = RecipeIngredient(
                id = "ri-1",
                recipeId = recipeId,
                ingredientId = "ingredient-1",
                createdAt = "2025-01-01T00:00:00Z",
                ingredient = ingredient
            )

            coEvery { repository.findById(recipeId) } returns recipe
            coEvery { repository.findIngredientsByRecipeId(recipeId) } returns listOf(recipeIngredient)

            val result = recipeService.getRecipeById(recipeId)

            assertNotNull(result)
            assertEquals("Pasta", result.name)
            assertEquals(1, result.ingredients.size)
            assertEquals("Tomato", result.ingredients[0].name)

            coVerify { repository.findById(recipeId) }
            coVerify { repository.findIngredientsByRecipeId(recipeId) }
        }

        @Test
        fun `should return null when recipe not found`() = runTest {
            val recipeId = "nonexistent-id"

            coEvery { repository.findById(recipeId) } returns null

            val result = recipeService.getRecipeById(recipeId)

            assertNull(result)
            coVerify { repository.findById(recipeId) }
            coVerify(exactly = 0) { repository.findIngredientsByRecipeId(any()) }
        }
    }

    @Nested
    @DisplayName("createRecipe")
    inner class CreateRecipe {

        @Test
        fun `should create recipe with ingredients successfully`() = runTest {
            val request = CreateRecipeRequest(
                name = "Pasta",
                description = "Delicious pasta",
                prepTimeMinutes = 10,
                cookTimeMinutes = 20,
                servings = 4,
                difficulty = "Easy",
                ingredientIds = listOf("ingredient-1", "ingredient-2")
            )
            val recipeId = "recipe-id"
            val createdRecipe = Recipe(
                id = recipeId,
                name = request.name,
                description = request.description,
                prepTimeMinutes = request.prepTimeMinutes,
                cookTimeMinutes = request.cookTimeMinutes,
                servings = request.servings,
                difficulty = request.difficulty,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val ingredient1 = Ingredient(
                id = "ingredient-1",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val ingredient2 = Ingredient(
                id = "ingredient-2",
                name = "Pasta",
                category = "Grain",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val recipeIngredients = listOf(
                RecipeIngredient(
                    id = "ri-1",
                    recipeId = recipeId,
                    ingredientId = "ingredient-1",
                    createdAt = "2025-01-01T00:00:00Z",
                    ingredient = ingredient1
                ),
                RecipeIngredient(
                    id = "ri-2",
                    recipeId = recipeId,
                    ingredientId = "ingredient-2",
                    createdAt = "2025-01-01T00:00:00Z",
                    ingredient = ingredient2
                )
            )

            coEvery { repository.create(any()) } returns recipeId
            coEvery { repository.addIngredientToRecipe(any()) } returns mockk()
            coEvery { repository.findById(recipeId) } returns createdRecipe
            coEvery { repository.findIngredientsByRecipeId(recipeId) } returns recipeIngredients

            val result = recipeService.createRecipe(request)

            assertEquals("Pasta", result.name)
            assertEquals(2, result.ingredients.size)
            assertEquals("Tomato", result.ingredients[0].name)
            assertEquals("Pasta", result.ingredients[1].name)

            coVerify { repository.create(any()) }
            coVerify(exactly = 2) { repository.addIngredientToRecipe(any()) }
            coVerify { repository.findById(recipeId) }
            coVerify { repository.findIngredientsByRecipeId(recipeId) }
        }

        @Test
        fun `should create recipe without ingredients`() = runTest {
            val request = CreateRecipeRequest(
                name = "Pasta",
                description = "Delicious pasta",
                prepTimeMinutes = 10,
                cookTimeMinutes = 20,
                servings = 4,
                difficulty = "Easy",
                ingredientIds = null
            )
            val recipeId = "recipe-id"
            val createdRecipe = Recipe(
                id = recipeId,
                name = request.name,
                description = request.description,
                prepTimeMinutes = request.prepTimeMinutes,
                cookTimeMinutes = request.cookTimeMinutes,
                servings = request.servings,
                difficulty = request.difficulty,
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { repository.create(any()) } returns recipeId
            coEvery { repository.findById(recipeId) } returns createdRecipe
            coEvery { repository.findIngredientsByRecipeId(recipeId) } returns emptyList()

            val result = recipeService.createRecipe(request)

            assertEquals("Pasta", result.name)
            assertEquals(0, result.ingredients.size)

            coVerify { repository.create(any()) }
            coVerify(exactly = 0) { repository.addIngredientToRecipe(any()) }
        }

        @Test
        fun `should throw exception when recipe creation fails`() = runTest {
            val request = CreateRecipeRequest(
                name = "Pasta",
                description = "Delicious pasta",
                prepTimeMinutes = 10,
                cookTimeMinutes = 20,
                servings = 4,
                difficulty = "Easy",
                ingredientIds = null
            )
            val recipeId = "recipe-id"

            coEvery { repository.create(any()) } returns recipeId
            coEvery { repository.findById(recipeId) } returns null

            assertThrows<IllegalStateException> {
                recipeService.createRecipe(request)
            }
        }
    }

    @Nested
    @DisplayName("updateRecipe")
    inner class UpdateRecipe {

        @Test
        fun `should update recipe successfully`() = runTest {
            val recipe = Recipe(
                id = "recipe-1",
                name = "Updated Pasta",
                description = "Updated description",
                prepTimeMinutes = 15,
                cookTimeMinutes = 25,
                servings = 6,
                difficulty = "Medium",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val ingredient = Ingredient(
                id = "ingredient-1",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val recipeIngredient = RecipeIngredient(
                id = "ri-1",
                recipeId = "recipe-1",
                ingredientId = "ingredient-1",
                createdAt = "2025-01-01T00:00:00Z",
                ingredient = ingredient
            )

            coEvery { repository.update(any()) } returns 1
            coEvery { repository.findById("recipe-1") } returns recipe
            coEvery { repository.findIngredientsByRecipeId("recipe-1") } returns listOf(recipeIngredient)

            val result = recipeService.updateRecipe(recipe)

            assertNotNull(result)
            assertEquals("Updated Pasta", result.name)
            assertEquals(1, result.ingredients.size)

            coVerify { repository.update(any()) }
            coVerify { repository.findById("recipe-1") }
            coVerify { repository.findIngredientsByRecipeId("recipe-1") }
        }

        @Test
        fun `should return null when recipe not found`() = runTest {
            val recipe = Recipe(
                id = "nonexistent-id",
                name = "Pasta",
                description = "Description",
                prepTimeMinutes = 10,
                cookTimeMinutes = 20,
                servings = 4,
                difficulty = "Easy",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )

            coEvery { repository.update(any()) } returns 0

            val result = recipeService.updateRecipe(recipe)

            assertNull(result)
            coVerify { repository.update(any()) }
            coVerify(exactly = 0) { repository.findById(any()) }
        }
    }

    @Nested
    @DisplayName("deleteRecipe")
    inner class DeleteRecipe {

        @Test
        fun `should delete recipe successfully`() = runTest {
            val recipeId = "recipe-1"

            coEvery { repository.delete(recipeId) } returns true

            val result = recipeService.deleteRecipe(recipeId)

            assertTrue(result)
            coVerify { repository.delete(recipeId) }
        }

        @Test
        fun `should return false when recipe not found`() = runTest {
            val recipeId = "nonexistent-id"

            coEvery { repository.delete(recipeId) } returns false

            val result = recipeService.deleteRecipe(recipeId)

            assertEquals(false, result)
            coVerify { repository.delete(recipeId) }
        }
    }

    @Nested
    @DisplayName("getIngredientsForRecipe")
    inner class GetIngredientsForRecipe {

        @Test
        fun `should return ingredients for recipe`() = runTest {
            val recipeId = "recipe-1"
            val ingredient = Ingredient(
                id = "ingredient-1",
                name = "Tomato",
                category = "Vegetable",
                createdAt = "2025-01-01T00:00:00Z",
                updatedAt = "2025-01-01T00:00:00Z"
            )
            val recipeIngredient = RecipeIngredient(
                id = "ri-1",
                recipeId = recipeId,
                ingredientId = "ingredient-1",
                createdAt = "2025-01-01T00:00:00Z",
                ingredient = ingredient
            )

            coEvery { repository.findIngredientsByRecipeId(recipeId) } returns listOf(recipeIngredient)

            val result = recipeService.getIngredientsForRecipe(recipeId)

            assertEquals(1, result.size)
            assertEquals("Tomato", result[0].ingredient?.name)

            coVerify { repository.findIngredientsByRecipeId(recipeId) }
        }
    }

    @Nested
    @DisplayName("addIngredientToRecipe")
    inner class AddIngredientToRecipe {

        @Test
        fun `should add ingredient to recipe successfully`() = runTest {
            val recipeIngredient = RecipeIngredient(
                id = "ri-1",
                recipeId = "recipe-1",
                ingredientId = "ingredient-1",
                createdAt = "2025-01-01T00:00:00Z"
            )

            coEvery { repository.addIngredientToRecipe(recipeIngredient) } returns recipeIngredient

            val result = recipeService.addIngredientToRecipe(recipeIngredient)

            assertEquals(recipeIngredient, result)
            coVerify { repository.addIngredientToRecipe(recipeIngredient) }
        }
    }

    @Nested
    @DisplayName("removeIngredientFromRecipe")
    inner class RemoveIngredientFromRecipe {

        @Test
        fun `should remove ingredient from recipe successfully`() = runTest {
            val recipeIngredientId = "ri-1"

            coEvery { repository.removeIngredientFromRecipe(recipeIngredientId) } returns true

            val result = recipeService.removeIngredientFromRecipe(recipeIngredientId)

            assertTrue(result)
            coVerify { repository.removeIngredientFromRecipe(recipeIngredientId) }
        }

        @Test
        fun `should return false when ingredient not found`() = runTest {
            val recipeIngredientId = "nonexistent-id"

            coEvery { repository.removeIngredientFromRecipe(recipeIngredientId) } returns false

            val result = recipeService.removeIngredientFromRecipe(recipeIngredientId)

            assertEquals(false, result)
            coVerify { repository.removeIngredientFromRecipe(recipeIngredientId) }
        }
    }
}
