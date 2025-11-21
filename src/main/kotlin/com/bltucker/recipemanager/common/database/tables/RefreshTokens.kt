package com.bltucker.recipemanager.common.database.tables

import kotlinx.datetime.Instant
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.LongIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object RefreshTokens : LongIdTable("refresh_tokens") {
    val userId = reference("user_id", Users.id)
    val token = varchar("token", 512)
    val expiresAt: Column<Instant> = timestamp("expires_at")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex("idx_refresh_tokens_token", token)

        index("idx_refresh_tokens_user_id", isUnique = false, userId)
    }
}
