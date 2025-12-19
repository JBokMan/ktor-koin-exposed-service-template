package com.example.boundary.pokemon

import boundary.pokemon.Pokemon
import boundary.pokemon.PokemonListResponse
import boundary.pokemon.PokemonSpecies
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PokemonApi {
    @GET("pokemon/")
    fun getPokemonList(
        @Query("offset") offset: Int? = null,
        @Query("limit") limit: Int? = null,
    ): Call<PokemonListResponse>

    @GET("pokemon/{idOrName}/") fun getPokemon(@Path("idOrName") idOrName: String): Call<Pokemon>

    @GET("pokemon-species/{idOrName}/")
    fun getPokemonSpecies(@Path("idOrName") idOrName: String): Call<PokemonSpecies>
}
