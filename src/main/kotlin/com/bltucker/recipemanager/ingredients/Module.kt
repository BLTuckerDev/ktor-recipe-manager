package com.bltucker.recipemanager.ingredients

import com.bltucker.recipemanager.common.models.Ingredient
import com.bltucker.recipemanager.common.repositories.IngredientRepository
import com.bltucker.recipemanager.common.plugins.UserContext
import kotlinx.coroutines.withContext
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing


fun Application.ingredientsModule(){
    dependencies{
        provide<IngredientRepository> { ExposedIngredientRepository() }
        provide(IngredientService::class)
    }

    routing{
        ingredientRoutes()
    }
}

private fun Route.ingredientRoutes(){
    authenticate("auth-jwt") {
        route("/api/v1/ingredients"){

            get {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.payload.getClaim("userId").asString()
                val ingredientService = call.application.dependencies.resolve<IngredientService>()

                withContext(UserContext(userId)) {
                    val ingredients = ingredientService.getAllIngredients()
                    call.respond(ingredients)
                }
            }

        post {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asString()
            val ingredientService = call.application.dependencies.resolve<IngredientService>()
            val request = call.receive<CreateIngredientRequest>()

            val ingredient = Ingredient(
                id = "", // Service will set this
                name = request.name,
                description = request.description,
                category = request.category,
                defaultUnit = request.defaultUnit,
                createdAt = "", // Service will set this
                updatedAt = ""  // Service will set this
            )

            withContext(UserContext(userId)) {
                val created = ingredientService.createIngredient(ingredient)
                call.respond(HttpStatusCode.Created, created)
            }
        }

        get("/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asString()
            val ingredientService = call.application.dependencies.resolve<IngredientService>()
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing ingredient ID")
            )

            withContext(UserContext(userId)) {
                val ingredient = ingredientService.getIngredientById(id)
                if (ingredient != null) {
                    call.respond(ingredient)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Ingredient not found"))
                }
            }
        }

        put("/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asString()
            val ingredientService = call.application.dependencies.resolve<IngredientService>()
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing ingredient ID")
            )

            withContext(UserContext(userId)) {
                val request = call.receive<UpdateIngredientRequest>()
                val existingIngredient = ingredientService.getIngredientById(id) ?: return@withContext call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Ingredient not found")
                )

                val updatedIngredient = existingIngredient.copy(
                    name = request.name ?: existingIngredient.name,
                    description = request.description ?: existingIngredient.description,
                    category = request.category ?: existingIngredient.category,
                    defaultUnit = request.defaultUnit ?: existingIngredient.defaultUnit
                )

                val result = ingredientService.updateIngredient(updatedIngredient)
                if (result != null) {
                    call.respond(result)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update ingredient"))
                }
            }
        }

        delete("/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal!!.payload.getClaim("userId").asString()
            val ingredientService = call.application.dependencies.resolve<IngredientService>()
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing ingredient ID")
            )

            withContext(UserContext(userId)) {
                val deleted = ingredientService.deleteIngredient(id)
                if (deleted) {
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Ingredient not found"))
                }
            }
        }


        }
    }
}