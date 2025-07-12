package com.bltucker.recipemanager.recipes

import com.bltucker.recipemanager.common.models.Recipe
import com.bltucker.recipemanager.common.models.RecipeRepository
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
        provide<RecipeService> { RecipeService(resolve()) }
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

    // GET /api/v1/recipes - List all recipes
    get {
        val recipeService = call.application.dependencies.resolve<RecipeService>()
        val recipes = recipeService.getAllRecipes()
        call.respond(recipes)
    }

    // POST /api/v1/recipes - Create new recipe
    post {
        val recipeService = call.application.dependencies.resolve<RecipeService>()
        val request = call.receive<CreateRecipeRequest>()

        val recipe = Recipe(
            id = "",
            name = request.name,
            description = request.description,
            prepTimeMinutes = request.prepTimeMinutes,
            cookTimeMinutes = request.cookTimeMinutes,
            servings = request.servings,
            difficulty = request.difficulty,
            createdAt = "",
            updatedAt = ""
        )

        val created = recipeService.createRecipe(recipe)
        call.respond(HttpStatusCode.Created, created)
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
        val existingRecipe = recipeService.getRecipeById(id) ?: return@put call.respond(
            HttpStatusCode.NotFound,
            mapOf("error" to "Recipe not found")
        )

        val updatedRecipe = existingRecipe.copy(
            name = request.name ?: existingRecipe.name,
            description = request.description ?: existingRecipe.description,
            prepTimeMinutes = request.prepTimeMinutes ?: existingRecipe.prepTimeMinutes,
            cookTimeMinutes = request.cookTimeMinutes ?: existingRecipe.cookTimeMinutes,
            servings = request.servings ?: existingRecipe.servings,
            difficulty = request.difficulty ?: existingRecipe.difficulty
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