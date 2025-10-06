package com.bltucker.recipemanager.ingredients

import com.bltucker.recipemanager.common.UserContextProvider
import com.bltucker.recipemanager.common.testing.DatabaseTestBase
import com.bltucker.recipemanager.common.testing.TestConstants
import com.bltucker.recipemanager.common.testing.TestDataFactory
import io.mockk.coEvery
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("ExposedIngredientRepository")
class ExposedIngredientRepositoryTest : DatabaseTestBase() {
    
    private lateinit var repository: ExposedIngredientRepository
    private lateinit var mockUserContextProvider: UserContextProvider
    override fun afterDatabaseSetup() {
        mockUserContextProvider = mockk()

        coEvery { mockUserContextProvider.getUserId() } returns TestConstants.TEST_USER_ID.toString()

        repository = ExposedIngredientRepository(mockUserContextProvider)
    }
    
    @Nested
    @DisplayName("Create operations")
    inner class CreateOperations {
        
        @Test
        fun `should create ingredient and return generated ID`() = runTest {
            val ingredient = TestDataFactory.createTomato()
            
            val createdId = repository.create(ingredient)
            
            assertThat(createdId).isNotBlank()
            val found = repository.findById(createdId)
            assertNotNull(found)
            assertEquals("Tomato", found.name)
            assertEquals("Vegetable", found.category)
        }
        
        @Test
        fun `should create ingredient with minimal data`() = runTest {
            val ingredient = TestDataFactory.createIngredient(
                name = "Minimal Ingredient",
                category = null,
                description = null,
                defaultUnit = null
            )
            
            val createdId = repository.create(ingredient)
            val found = repository.findById(createdId)
            
            assertNotNull(found)
            assertEquals("Minimal Ingredient", found.name)
            assertNull(found.category)
            assertNull(found.description)
            assertNull(found.defaultUnit)
        }
    }
    
    @Nested
    @DisplayName("Read operations")
    inner class ReadOperations {
        
        @Test
        fun `should find ingredient by ID`() = runTest {
            val ingredient = TestDataFactory.createCarrot()
            val createdId = repository.create(ingredient)
            
            val found = repository.findById(createdId)
            
            assertNotNull(found)
            assertEquals(createdId, found.id)
            assertEquals("Carrot", found.name)
        }
        
        @Test
        fun `should return null when ingredient not found`() = runTest {
            val found = repository.findById("550e8400-e29b-41d4-a716-446655440000")
            assertNull(found)
        }
        
        @Test
        fun `should find all ingredients`() = runTest {
            repository.create(TestDataFactory.createTomato())
            repository.create(TestDataFactory.createCarrot())
            repository.create(TestDataFactory.createSalt())
            
            val all = repository.findAll()
            
            assertEquals(3, all.size)
            assertThat(all.map { it.name }).containsExactlyInAnyOrder("Tomato", "Carrot", "Salt")
        }
        
        @Test
        fun `should filter ingredients by category`() = runTest {
            repository.create(TestDataFactory.createTomato())
            repository.create(TestDataFactory.createCarrot())
            repository.create(TestDataFactory.createSalt())
            
            val vegetables = repository.findAll(category = "Vegetable")
            
            assertEquals(2, vegetables.size)
            assertThat(vegetables.map { it.name }).containsExactlyInAnyOrder("Tomato", "Carrot")
        }
        
        @Test
        fun `should search ingredients by name case-insensitive`() = runTest {
            val tomato = TestDataFactory.createTomato()
            val tomatoSauce = TestDataFactory.createIngredient(
                name = "Tomato Sauce",
                category = "Condiment"
            )
            val carrot = TestDataFactory.createCarrot()
            
            repository.create(tomato)
            repository.create(tomatoSauce)
            repository.create(carrot)
            
            val results = repository.searchByName("tomato")
            
            assertEquals(2, results.size)
            assertThat(results.map { it.name }).containsExactlyInAnyOrder("Tomato", "Tomato Sauce")
        }
        
        @Test
        fun `should return empty list when no matches found in search`() = runTest {
            repository.create(TestDataFactory.createTomato())
            
            val results = repository.searchByName("nonexistent")
            
            assertTrue(results.isEmpty())
        }
        
        @Test
        fun `should filter by search term in findAll`() = runTest {
            repository.create(TestDataFactory.createTomato())
            repository.create(TestDataFactory.createCarrot())
            repository.create(TestDataFactory.createSalt())
            
            val results = repository.findAll(searchTerm = "tom")
            
            assertEquals(1, results.size)
            assertEquals("Tomato", results.first().name)
        }
        
        @Test
        fun `should filter by category and search term in findAll`() = runTest {
            val tomato = TestDataFactory.createTomato()
            val tomatoSauce = TestDataFactory.createIngredient(
                name = "Tomato Sauce",
                category = "Condiment"
            )
            val carrot = TestDataFactory.createCarrot()
            
            repository.create(tomato)
            repository.create(tomatoSauce)
            repository.create(carrot)
            
            val results = repository.findAll(category = "Vegetable", searchTerm = "tom")
            
            assertEquals(1, results.size)
            assertEquals("Tomato", results.first().name)
        }
    }
    
    @Nested
    @DisplayName("Update operations")
    inner class UpdateOperations {
        
        @Test
        fun `should update ingredient successfully`() = runTest {
            val ingredient = TestDataFactory.createTomato()
            val createdId = repository.create(ingredient)
            
            val updated = ingredient.copy(
                id = createdId,
                name = "Cherry Tomato",
                description = "Small sweet tomato"
            )
            
            val result = repository.update(updated)
            
            assertNotNull(result)
            assertEquals("Cherry Tomato", result.name)
            assertEquals("Small sweet tomato", result.description)
            assertEquals("Vegetable", result.category)
        }
        
        @Test
        fun `should return null when updating non-existent ingredient`() = runTest {
            val ingredient = TestDataFactory.createTomato().copy(id = "550e8400-e29b-41d4-a716-446655440000")
            
            val result = repository.update(ingredient)
            
            assertNull(result)
        }
        
        @Test
        fun `should update only provided fields`() = runTest {
            val ingredient = TestDataFactory.createTomato()
            val createdId = repository.create(ingredient)
            
            val updated = ingredient.copy(
                id = createdId,
                name = "Updated Tomato",
                category = null
            )
            
            val result = repository.update(updated)
            
            assertNotNull(result)
            assertEquals("Updated Tomato", result.name)
            assertEquals("Vegetable", result.category)
        }
    }
    
    @Nested
    @DisplayName("Delete operations")
    inner class DeleteOperations {
        
        @Test
        fun `should delete ingredient successfully`() = runTest {
            val ingredient = TestDataFactory.createTomato()
            val createdId = repository.create(ingredient)
            
            val deleted = repository.delete(createdId)
            
            assertTrue(deleted)
            val found = repository.findById(createdId)
            assertNull(found)
        }
        
        @Test
        fun `should return false when deleting non-existent ingredient`() = runTest {
            val deleted = repository.delete("550e8400-e29b-41d4-a716-446655440000")
            assertThat(deleted).isFalse()
        }
    }
    
    @Nested
    @DisplayName("Complex queries")
    inner class ComplexQueries {
        
        @Test
        fun `should find most used ingredients`() = runTest {
            //TODO later come back to this
            // This test would require setting up recipe_ingredients data
            // For now, test that it doesn't crash with empty data
            val mostUsed = repository.findMostUsed(5)
            assertTrue(mostUsed.isEmpty())
        }
    }
}