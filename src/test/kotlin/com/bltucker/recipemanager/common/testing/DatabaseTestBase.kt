package com.bltucker.recipemanager.common.testing

import com.bltucker.recipemanager.common.database.tables.Ingredients
import com.bltucker.recipemanager.common.database.tables.RecipeIngredients
import com.bltucker.recipemanager.common.database.tables.Recipes
import com.bltucker.recipemanager.common.database.tables.Users
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class DatabaseTestBase {
    
    protected lateinit var database: Database
    
    @BeforeEach
    fun setupDatabase() {
        database = Database.connect(
            url = "jdbc:h2:mem:test_${System.currentTimeMillis()};DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
            driver = "org.h2.Driver"
        )
        
        transaction(database) {
            SchemaUtils.create(Users, Recipes, Ingredients, RecipeIngredients)
        }
        
        afterDatabaseSetup()
    }
    
    @AfterEach
    fun teardownDatabase() {
        transaction(database) {
            SchemaUtils.drop(RecipeIngredients, Ingredients, Recipes, Users)
        }
    }
    
    protected open fun afterDatabaseSetup() {}
    
    protected fun runTest(block: suspend () -> Unit) {
        kotlinx.coroutines.runBlocking {
            block()
        }
    }
}