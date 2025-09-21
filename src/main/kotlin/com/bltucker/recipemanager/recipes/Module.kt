package com.bltucker.recipemanager.recipes

import com.bltucker.recipemanager.common.models.Recipe
import com.bltucker.recipemanager.common.models.RecipeIngredient
import com.bltucker.recipemanager.common.repositories.RecipeRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.html.respondHtml
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
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.head
import kotlinx.html.p
import kotlinx.html.script
import kotlinx.html.title


fun Application.recipesModule() {
    dependencies {
        provide<RecipeRepository> { ExposedRecipeRepository() }
        provide(RecipeService::class)
    }

    routing{
        webRoutes()
        apiRoutes()
    }

}

private fun Route.webRoutes() {
    get("/") {
        call.respondHtml {
            head {
                title("Recipe Manager")
                script(src = "https://cdn.tailwindcss.com") {}
            }
            body("bg-gray-100 min-h-screen") {
                div("container mx-auto px-4 py-8") {
                    div("bg-white rounded-lg shadow-md p-6") {
                        h1("text-3xl font-bold text-gray-800 mb-4") {
                            +"🍳 Recipe Manager"
                        }
                        p("text-gray-600 mb-4") {
                            +"Welcome to your personal recipe management system!"
                        }
                        div("bg-blue-50 p-4 rounded") {
                            p("text-blue-800") {
                                +"Built with Ktor and Kotlin"
                            }
                        }
                    }
                }
            }
        }
    }
}


private fun Route.apiRoutes(){
    route("/api/v1/recipes"){
        // GET /api/v1/recipes - List all recipes
        get {
            val recipeService = call.application.dependencies.resolve<RecipeService>()
            val recipes = recipeService.getAllRecipes()
            call.respond(recipes)
        }

        // POST /api/v1/recipes - Create new recipe
        post {
            try {
                val recipeService = call.application.dependencies.resolve<RecipeService>()
                val request: CreateRecipeRequest = call.receive<CreateRecipeRequest>()

                println("Received request: $request")


                val created = recipeService.createRecipe(request)
                println("Created recipe: $created")

                call.respond(HttpStatusCode.Created, created)
            } catch (e: Exception) {
                println("Error creating recipe: ${e.message}")
                e.printStackTrace()
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }

        // GET /api/v1/recipes/{id} - Get recipe by ID
        get("/{id}") {
            val recipeService = call.application.dependencies.resolve<RecipeService>()
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing recipe ID")
            )

            val recipe = recipeService.getRecipeById(id)
            if (recipe != null) {
                call.respond(recipe)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Recipe not found"))
            }
        }

        // PUT /api/v1/recipes/{id} - Update recipe
        put("/{id}") {
            val recipeService = call.application.dependencies.resolve<RecipeService>()
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing recipe ID")
            )

            val request = call.receive<UpdateRecipeRequest>()
            val existingRecipeResponse = recipeService.getRecipeById(id) ?: return@put call.respond(
                HttpStatusCode.NotFound,
                mapOf("error" to "Recipe not found")
            )

            val updatedRecipe = Recipe(
                id = existingRecipeResponse.id,
                name = request.name ?: existingRecipeResponse.name,
                description = request.description ?: existingRecipeResponse.description,
                prepTimeMinutes = request.prepTimeMinutes ?: existingRecipeResponse.prepTimeMinutes,
                cookTimeMinutes = request.cookTimeMinutes ?: existingRecipeResponse.cookTimeMinutes,
                servings = request.servings ?: existingRecipeResponse.servings,
                difficulty = request.difficulty ?: existingRecipeResponse.difficulty,
                createdAt = existingRecipeResponse.createdAt,
                updatedAt = existingRecipeResponse.updatedAt // The service will update this
            )

            val result = recipeService.updateRecipe(updatedRecipe)
            if (result != null) {
                call.respond(result)
            } else {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to "Failed to update recipe"))
            }
        }

        // DELETE /api/v1/recipes/{id} - Delete recipe
        delete("/{id}") {
            val recipeService = call.application.dependencies.resolve<RecipeService>()
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing recipe ID")
            )

            val deleted = recipeService.deleteRecipe(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Recipe not found"))
            }
        }
    }

    route("/api/v1/recipes/{recipeId}/ingredients") {

        // GET all ingredients for a recipe
        get {
            val recipeService = call.application.dependencies.resolve<RecipeService>()
            val recipeId = call.parameters["recipeId"] ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing recipe ID")
            )

            val ingredients = recipeService.getIngredientsForRecipe(recipeId)
            call.respond(ingredients)
        }

        // POST a new ingredient to a recipe
        post {
            val recipeService = call.application.dependencies.resolve<RecipeService>()
            val recipeId = call.parameters["recipeId"] ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing recipe ID")
            )

            val request = call.receive<AddIngredientRequest>()

            val recipeIngredient = RecipeIngredient(
                id = "", // Will be set by the database
                recipeId = recipeId,
                ingredientId = request.ingredientId,
                quantity = request.quantity,
                unit = request.unit,
                notes = request.notes,
                createdAt = "" // Will be set by the database
            )

            val created = recipeService.addIngredientToRecipe(recipeIngredient)
            call.respond(HttpStatusCode.Created, created)
        }
    }

    // New route to delete a recipe ingredient
    route("/api/v1/recipe-ingredients/{id}"){
        delete {
            val recipeService = call.application.dependencies.resolve<RecipeService>()
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing recipe ingredient ID")
            )

            val deleted = recipeService.removeIngredientFromRecipe(id)
            if(deleted) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "Recipe ingredient not found"))
            }
        }
    }
}