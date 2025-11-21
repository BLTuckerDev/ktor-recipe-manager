package com.bltucker.recipemanager.common.database.tables

import org.jetbrains.exposed.v1.core.dao.id.UUIDTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.timestamp


object Recipes : UUIDTable("recipes") {
    val userId = reference("user_id", Users.id)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val prepTimeMinutes = integer("prep_time_minutes").nullable()
    val cookTimeMinutes = integer("cook_time_minutes").nullable()
    val servings = integer("servings").nullable()
    val difficulty = varchar("difficulty", 20).nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)
}