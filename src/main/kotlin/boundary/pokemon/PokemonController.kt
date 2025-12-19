package boundary.pokemon

import com.example.config.PokemonClient
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktoropenapi.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import org.koin.core.annotation.Single
import retrofit2.await

@Single
class PokemonController(private val pokemonClient: PokemonClient) {
    private val log = KotlinLogging.logger { this::class::simpleName }

    fun registerRoutes(app: Application) {
        app.routing {
            get(
                "/api/pokemon",
                {
                    summary = "Get Pokemon list"
                    description = "Retrieves a paginated list of Pokemon"
                    request {
                        queryParameter<Int?>("offset") { description = "Pagination offset" }
                        queryParameter<Int?>("limit") { description = "Number of items to return" }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            description = "List of Pokemon retrieved successfully"
                            body<PokemonListResponse>()
                        }
                        code(HttpStatusCode.InternalServerError) {
                            description = "Error fetching data from Pokemon API"
                            body<String>()
                        }
                    }
                },
            ) {
                try {
                    val offset = call.request.queryParameters["offset"]?.toIntOrNull()
                    val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
                    val response = pokemonClient.getPokemonList(offset, limit).await()
                    call.respond(response)
                } catch (e: Exception) {
                    log.error(e) { "Error fetching Pokemon list from Pokemon API" }
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error fetching data from Pokemon API",
                    )
                }
            }

            get(
                "/api/pokemon/{idOrName}",
                {
                    summary = "Get Pokemon by ID or name"
                    description = "Retrieves a specific Pokemon by its ID or name"
                    request {
                        pathParameter<String>("idOrName") { description = "Pokemon ID or name" }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            description = "Pokemon retrieved successfully"
                            body<Pokemon>()
                        }
                        code(HttpStatusCode.BadRequest) {
                            description = "Invalid ID or name format"
                            body<String>()
                        }
                        code(HttpStatusCode.InternalServerError) {
                            description = "Error fetching data from Pokemon API"
                            body<String>()
                        }
                    }
                },
            ) {
                try {
                    val idOrName =
                        call.parameters["idOrName"]
                            ?: run {
                                call.respond(HttpStatusCode.BadRequest, "Invalid ID or name format")
                                return@get
                            }

                    val pokemon = pokemonClient.getPokemon(idOrName).await()
                    call.respond(pokemon)
                } catch (e: Exception) {
                    log.error(e) {
                        "Error fetching Pokemon with ID/name ${call.parameters["idOrName"]} from Pokemon API"
                    }
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error fetching data from Pokemon API",
                    )
                }
            }

            get(
                "/api/pokemon-species/{idOrName}",
                {
                    summary = "Get Pokemon species by ID or name"
                    description = "Retrieves a specific Pokemon species by its ID or name"
                    request {
                        pathParameter<String>("idOrName") {
                            description = "Pokemon species ID or name"
                        }
                    }
                    response {
                        code(HttpStatusCode.OK) {
                            description = "Pokemon species retrieved successfully"
                            body<PokemonSpecies>()
                        }
                        code(HttpStatusCode.BadRequest) {
                            description = "Invalid ID or name format"
                            body<String>()
                        }
                        code(HttpStatusCode.InternalServerError) {
                            description = "Error fetching data from Pokemon API"
                            body<String>()
                        }
                    }
                },
            ) {
                try {
                    val idOrName =
                        call.parameters["idOrName"]
                            ?: run {
                                call.respond(HttpStatusCode.BadRequest, "Invalid ID or name format")
                                return@get
                            }

                    val species = pokemonClient.getPokemonSpecies(idOrName).await()
                    call.respond(species)
                } catch (e: Exception) {
                    log.error(e) {
                        "Error fetching Pokemon species with ID/name ${call.parameters["idOrName"]} from Pokemon API"
                    }
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        "Error fetching data from Pokemon API",
                    )
                }
            }
        }
    }
}
