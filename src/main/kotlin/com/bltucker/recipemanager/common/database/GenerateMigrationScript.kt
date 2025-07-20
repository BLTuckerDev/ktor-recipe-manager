package com.bltucker.recipemanager.common.database

import com.bltucker.recipemanager.common.database.tables.Ingredients
import com.bltucker.recipemanager.common.database.tables.RecipeIngredients
import com.bltucker.recipemanager.common.database.tables.Recipes
import com.bltucker.recipemanager.common.models.RecipeIngredient
import com.typesafe.config.ConfigFactory
import org.jetbrains.exposed.v1.core.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.migration.MigrationUtils

fun main(){
    val config = ConfigFactory.load()

    println("Generating migration script...")
    println("config: $config")

    val dataSource = provideDataSource(
        DatasourceConfig(
            host = config.getString("database.host"),
            port = config.getString("database.port"),
            name = config.getString("database.name"),
            username = config.getString("database.username"),
            password = config.getString("database.password"),
            maxPoolSize = config.getInt("database.maxPoolSize"),
            sslMode = config.getString("database.sslMode")
        )
    )

    val db = Database.connect(dataSource)

    generateMigrationScript(db)
}

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun generateMigrationScript(db: Database) {


    transaction(db) {
        MigrationUtils.generateMigrationScript(
            tables = arrayOf(Recipes, Ingredients, RecipeIngredients),
            scriptDirectory = "migrations",
            scriptName = "V2__Ingredients"
        )
    }

}