package com.bltucker.recipemanager.common.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import io.ktor.server.plugins.di.annotations.Property
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.plugins.di.provide
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils

suspend fun Application.configureDatabase() {
    dependencies{
        provide(::provideDataSource)
    }

    val dataSource: HikariDataSource by dependencies

    Database.connect(dataSource)

}


data class DatasourceConfig(
    val host: String,
    val port: String,
    val name: String,
    val username: String,
    val password: String,
    val maxPoolSize: Int,
    val sslMode: String,
){
    val jdbcUrl = "jdbc:postgresql://$host:$port/$name?sslmode=$sslMode"
}

//provided via application.conf
fun provideDataSourceConfig(
    @Property("database.host") host: String,
    @Property("database.port") port: String,
    @Property("database.name") name: String,
    @Property("database.username") username: String,
    @Property("database.password") password: String,
    @Property("database.maxPoolSize") maxPoolSize: Int,
    @Property("database.sslMode") sslMode: String,
) = DatasourceConfig(host, port, name, username, password, maxPoolSize, sslMode)



fun provideDataSource(dbConfig: DatasourceConfig): HikariDataSource{
    val dataSource = HikariDataSource(HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        this.jdbcUrl = dbConfig.jdbcUrl
        username = dbConfig.username
        password = dbConfig.password
        maximumPoolSize = dbConfig.maxPoolSize
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"
        validate()
    })

    return dataSource
}
