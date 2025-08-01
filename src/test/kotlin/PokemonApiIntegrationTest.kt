package com.example

import boundary.pokemon.Pokemon
import boundary.pokemon.PokemonListResponse
import boundary.pokemon.PokemonSpecies
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class PokemonApiIntegrationTest : IntegrationTestBase() {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun testGetPokemonList() = withTestApp {
        // Act
        val response = client.get("/api/pokemon")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        val pokemonList = json.decodeFromString<PokemonListResponse>(responseBody)

        // Verify the structure of the response
        assertNotNull(pokemonList.count)
        assertNotNull(pokemonList.results)
        assertTrue(pokemonList.results.isNotEmpty())
    }

    @Test
    fun testGetPokemonListWithPagination() = withTestApp {
        // Act
        val response = client.get("/api/pokemon?offset=20&limit=10")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        val pokemonList = json.decodeFromString<PokemonListResponse>(responseBody)

        // Verify pagination parameters affected the response
        assertNotNull(pokemonList.count)
        assertNotNull(pokemonList.results)
        assertTrue(pokemonList.results.isNotEmpty())
        assertEquals(10, pokemonList.results.size)
    }

    @Test
    fun testGetPokemonById() = withTestApp {
        // Act
        val response = client.get("/api/pokemon/1")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        val pokemon = json.decodeFromString<Pokemon>(responseBody)

        // Verify the structure of the response
        assertEquals(1, pokemon.id)
        assertNotNull(pokemon.name)
        assertNotNull(pokemon.types)
        assertNotNull(pokemon.stats)
    }

    @Test
    fun testGetPokemonByName() = withTestApp {
        // Act
        val response = client.get("/api/pokemon/pikachu")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        val pokemon = json.decodeFromString<Pokemon>(responseBody)

        // Verify the structure of the response
        assertEquals("pikachu", pokemon.name)
        assertNotNull(pokemon.id)
        assertNotNull(pokemon.types)
        assertNotNull(pokemon.stats)
    }

    @Test
    fun testGetPokemonWithInvalidIdOrName() = withTestApp {
        // Act
        val response = client.get("/api/pokemon/invalid-pokemon-name")

        // Assert
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun testGetPokemonSpeciesById() = withTestApp {
        // Act
        val response = client.get("/api/pokemon-species/1")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        val species = json.decodeFromString<PokemonSpecies>(responseBody)

        // Verify the structure of the response
        assertEquals(1, species.id)
        assertNotNull(species.name)
        assertNotNull(species.generation)
        assertNotNull(species.flavorTextEntries)
    }

    @Test
    fun testGetPokemonSpeciesByName() = withTestApp {
        // Act
        val response = client.get("/api/pokemon-species/pikachu")

        // Assert
        assertEquals(HttpStatusCode.OK, response.status)
        val responseBody = response.bodyAsText()
        val species = json.decodeFromString<PokemonSpecies>(responseBody)

        // Verify the structure of the response
        assertEquals("pikachu", species.name)
        assertNotNull(species.id)
        assertNotNull(species.generation)
        assertNotNull(species.flavorTextEntries)
    }

    @Test
    fun testGetPokemonSpeciesWithInvalidIdOrName() = withTestApp {
        // Act
        val response = client.get("/api/pokemon-species/invalid-pokemon-name")

        // Assert
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }
}
