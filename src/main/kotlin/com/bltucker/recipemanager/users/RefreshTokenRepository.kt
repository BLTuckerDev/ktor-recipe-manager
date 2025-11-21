package com.bltucker.recipemanager.users

import com.bltucker.recipemanager.common.database.tables.RefreshTokens
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days

data class RefreshTokenRecord(
    val id: Long,
    val userId: UUID,
    val token: String,
    val expiresAt: Instant,
    val createdAt: Instant
)

interface RefreshTokenRepository {
    suspend fun save(userId: UUID, tokenHash: String, expiresAt: Instant)
    suspend fun findByTokenHash(tokenHash: String): RefreshTokenRecord?
    suspend fun deleteByUserId(userId: UUID)
    suspend fun deleteByTokenHash(tokenHash: String)
}

class ExposedRefreshTokenRepository : RefreshTokenRepository {
    override suspend fun save(userId: UUID, tokenHash: String, expiresAt: Instant) = transaction {
        RefreshTokens.insert {
            it[RefreshTokens.userId] = userId
            it[token] = tokenHash
            it[RefreshTokens.expiresAt] = Clock.System.now().plus(30.days)
        }
        Unit
    }

    override suspend fun findByTokenHash(tokenHash: String): RefreshTokenRecord? = transaction {
        RefreshTokens.selectAll()
            .where { RefreshTokens.token eq tokenHash }
            .firstOrNull()
            ?.let { row ->
                RefreshTokenRecord(
                    id = row[RefreshTokens.id].value,
                    userId = row[RefreshTokens.userId].value,
                    token = row[RefreshTokens.token],
                    expiresAt = row[RefreshTokens.expiresAt],
                    createdAt = row[RefreshTokens.createdAt],
                )
            }
    }

    override suspend fun deleteByUserId(userId: UUID) = transaction {
        RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
        Unit
    }

    override suspend fun deleteByTokenHash(tokenHash: String) = transaction {
        RefreshTokens.deleteWhere { RefreshTokens.token eq tokenHash }
        Unit
    }
}
