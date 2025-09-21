package com.bltucker.recipemanager.users

import com.bltucker.recipemanager.common.database.tables.Users
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.core.select
import org.jetbrains.exposed.v1.core.insert
import org.jetbrains.exposed.v1.core.update
import java.util.*

interface UserRepository {
    suspend fun createUser(email: String, hashedPassword: String): User
    suspend fun findByEmail(email: String): User?
    suspend fun findById(id: String): User?
    suspend fun markAsVerified(userId: String): Boolean
}

class ExposedUserRepository : UserRepository {

    override suspend fun createUser(email: String, hashedPassword: String): User = transaction {
        val userId = UUID.randomUUID()

        Users.insert {
            it[id] = userId
            it[Users.email] = email
            it[Users.hashedPassword] = hashedPassword
            it[isVerified] = false
        }

        User(
            id = userId.toString(),
            email = email,
            hashedPassword = hashedPassword,
            isVerified = false,
            createdAt = "",
            updatedAt = ""
        )
    }

    override suspend fun findByEmail(email: String): User? = transaction {
        Users.select { Users.email eq email }
            .firstOrNull()
            ?.let { row ->
                User(
                    id = row[Users.id].toString(),
                    email = row[Users.email],
                    hashedPassword = row[Users.hashedPassword],
                    isVerified = row[Users.isVerified],
                    createdAt = row[Users.createdAt].toString(),
                    updatedAt = row[Users.updatedAt].toString()
                )
            }
    }

    override suspend fun findById(id: String): User? = transaction {
        Users.select { Users.id eq UUID.fromString(id) }
            .firstOrNull()
            ?.let { row ->
                User(
                    id = row[Users.id].toString(),
                    email = row[Users.email],
                    hashedPassword = row[Users.hashedPassword],
                    isVerified = row[Users.isVerified],
                    createdAt = row[Users.createdAt].toString(),
                    updatedAt = row[Users.updatedAt].toString()
                )
            }
    }

    override suspend fun markAsVerified(userId: String): Boolean = transaction {
        Users.update({ Users.id eq UUID.fromString(userId) }) {
            it[isVerified] = true
        } > 0
    }
}