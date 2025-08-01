package boundary.pokemon

import com.example.config.PokemonClient
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

@Single
class PokemonController(private val pokemonClient: PokemonClient) {
    private val log = KotlinLogging.logger { this::class::simpleName }

    @GenerateOpenApi
    fun registerRoutes(app: Application) {
        app.routing {
            @KtorResponds(
                mapping =
                    [
                        ResponseEntry(
                            status = "200",
                            type = PokemonListResponse::class,
                            description = "List of Pokemon retrieved successfully",
                        ),
                        ResponseEntry(
                            status = "500",
                            type = String::class,
                            description = "Error fetching data from Pokemon API",
                        ),
                    ]
            )
            @KtorDescription(
                summary = "Get Pokemon list",
                description = "Retrieves a paginated list of Pokemon",
            )
            get("/api/pokemon") {
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

            @KtorResponds(
                mapping =
                    [
                        ResponseEntry(
                            status = "200",
                            type = Pokemon::class,
                            description = "Pokemon retrieved successfully",
                        ),
                        ResponseEntry(
                            status = "400",
                            type = String::class,
                            description = "Invalid ID or name format",
                        ),
                        ResponseEntry(
                            status = "500",
                            type = String::class,
                            description = "Error fetching data from Pokemon API",
                        ),
                    ]
            )
            @KtorDescription(
                summary = "Get Pokemon by ID or name",
                description = "Retrieves a specific Pokemon by its ID or name",
            )
            get("/api/pokemon/{idOrName}") {
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

            @KtorResponds(
                mapping =
                    [
                        ResponseEntry(
                            status = "200",
                            type = PokemonSpecies::class,
                            description = "Pokemon species retrieved successfully",
                        ),
                        ResponseEntry(
                            status = "400",
                            type = String::class,
                            description = "Invalid ID or name format",
                        ),
                        ResponseEntry(
                            status = "500",
                            type = String::class,
                            description = "Error fetching data from Pokemon API",
                        ),
                    ]
            )
            @KtorDescription(
                summary = "Get Pokemon species by ID or name",
                description = "Retrieves a specific Pokemon species by its ID or name",
            )
            get("/api/pokemon-species/{idOrName}") {
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
