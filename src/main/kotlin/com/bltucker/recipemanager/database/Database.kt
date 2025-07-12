package com.bltucker.recipemanager.database

import com.bltucker.recipemanager.database.tables.Recipes
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun Application.configureDatabase() {
    val dbHost = System.getenv("DB_HOST") ?: "localhost"
    val dbPort = System.getenv("DB_PORT") ?: "5432"
    val dbName = System.getenv("DB_NAME") ?: "recipemanager_dev"
    val dbUsername = System.getenv("DB_USERNAME") ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD") ?: ""
    val maxPoolSize = (System.getenv("DB_MAX_POOL_SIZE") ?: "10").toInt()
    val sslMode = System.getenv("DB_SSL_MODE") ?: "prefer"

    val jdbcUrl = "jdbc:postgresql://$dbHost:$dbPort/$dbName?sslmode=$sslMode"

    val dataSource = HikariDataSource(HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        this.jdbcUrl = jdbcUrl
        username = dbUsername
        password = dbPassword
        maximumPoolSize = maxPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })

    Database.connect(dataSource)

    transaction {
        SchemaUtils.create(Recipes)
    }
}