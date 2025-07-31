package com.example.boundary

import com.example.config.StarWarsClient
import com.example.model.PeopleResponse
import com.example.model.Person
import com.example.model.Planet
import com.example.model.PlanetResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.annotations.ResponseEntry
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.core.annotation.Single
import retrofit2.await

private val logger = KotlinLogging.logger {}

@Single
class StarWarsController(private val starWarsClient: StarWarsClient) {
    @GenerateOpenApi
    fun registerRoutes(app: Application) {
        app.routing {
            @KtorResponds(
                mapping =
                    [
                        ResponseEntry(
                            status = "200",
                            type = PeopleResponse::class,
                            description = "List of Star Wars people retrieved successfully",
                        ),
                        ResponseEntry(
                            status = "500",
                            type = String::class,
                            description = "Error fetching data from Star Wars API",
                        ),
                    ]
            )
            @KtorDescription(
                summary = "Get all Star Wars people",
                description = "Retrieves a paginated list of Star Wars people",
            )
            get("/api/people") {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull()
                    val response = starWarsClient.getPeople(page).await()
                    call.respond(response)
                } catch (e: Exception) {
                    logger.error(e) { "Error fetching people from Star Wars API" }
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error fetching data from Star Wars API",
                    )
                }
            }

            @KtorResponds(
                mapping =
                    [
                        ResponseEntry(
                            status = "200",
                            type = Person::class,
                            description = "Star Wars person retrieved successfully",
                        ),
                        ResponseEntry(
                            status = "400",
                            type = String::class,
                            description = "Invalid ID format",
                        ),
                        ResponseEntry(
                            status = "500",
                            type = String::class,
                            description = "Error fetching data from Star Wars API",
                        ),
                    ]
            )
            @KtorDescription(
                summary = "Get Star Wars person by ID",
                description = "Retrieves a specific Star Wars person by their ID",
            )
            get("/api/people/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                        return@get
                    }

                    val person = starWarsClient.getPerson(id).await()
                    call.respond(person)
                } catch (e: Exception) {
                    logger.error(e) {
                        "Error fetching person with ID ${call.parameters["id"]} from Star Wars API"
                    }
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error fetching data from Star Wars API",
                    )
                }
            }

            @KtorResponds(
                mapping =
                    [
                        ResponseEntry(
                            status = "200",
                            type = PlanetResponse::class,
                            description = "List of Star Wars planets retrieved successfully",
                        ),
                        ResponseEntry(
                            status = "500",
                            type = String::class,
                            description = "Error fetching data from Star Wars API",
                        ),
                    ]
            )
            @KtorDescription(
                summary = "Get all Star Wars planets",
                description = "Retrieves a paginated list of Star Wars planets",
            )
            get("/api/planets") {
                try {
                    val page = call.request.queryParameters["page"]?.toIntOrNull()
                    val response = starWarsClient.getPlanets(page).await()
                    call.respond(response)
                } catch (e: Exception) {
                    logger.error(e) { "Error fetching planets from Star Wars API" }
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error fetching data from Star Wars API",
                    )
                }
            }

            @KtorResponds(
                mapping =
                    [
                        ResponseEntry(
                            status = "200",
                            type = Planet::class,
                            description = "Star Wars planet retrieved successfully",
                        ),
                        ResponseEntry(
                            status = "400",
                            type = String::class,
                            description = "Invalid ID format",
                        ),
                        ResponseEntry(
                            status = "500",
                            type = String::class,
                            description = "Error fetching data from Star Wars API",
                        ),
                    ]
            )
            @KtorDescription(
                summary = "Get Star Wars planet by ID",
                description = "Retrieves a specific Star Wars planet by its ID",
            )
            get("/api/planets/{id}") {
                try {
                    val id = call.parameters["id"]?.toIntOrNull()
                    if (id == null) {
                        call.respond(HttpStatusCode.BadRequest, "Invalid ID format")
                        return@get
                    }

                    val planet = starWarsClient.getPlanet(id).await()
                    call.respond(planet)
                } catch (e: Exception) {
                    logger.error(e) {
                        "Error fetching planet with ID ${call.parameters["id"]} from Star Wars API"
                    }
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error fetching data from Star Wars API",
                    )
                }
            }
        }
    }
}
