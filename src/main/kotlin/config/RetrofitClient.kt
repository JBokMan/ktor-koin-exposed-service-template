package com.example.config

import com.example.boundary.pokemon.PokemonApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import org.koin.core.annotation.Single
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Converter
import retrofit2.Retrofit

val retrofitModule = module {
    single<Json> {
        Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
    }

    single<Converter.Factory> {
        val json = get<Json>()

        val contentType = "application/json".toMediaType()
        json.asConverterFactory(contentType)
    }

    single<OkHttpClient> {
        OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    // Named Retrofit instance for Pokemon API
    single<Retrofit>(qualifier = named("pokemonApi")) {
        val okHttpClient = get<OkHttpClient>()
        val converterFactory = get<Converter.Factory>()

        Retrofit.Builder()
            .baseUrl("https://pokeapi.co/api/v2/")
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }

    // Example of another Retrofit client with a different base URL
    // Uncomment and modify as needed
    /*
    single<Retrofit>(qualifier = named("anotherApi")) {
        val okHttpClient = get<OkHttpClient>()
        val converterFactory = get<Converter.Factory>()

        Retrofit.Builder()
            .baseUrl("https://api.example.com/")
            .client(okHttpClient)
            .addConverterFactory(converterFactory)
            .build()
    }
    */

    single<PokemonApi> {
        val retrofit = get<Retrofit>(qualifier = named("pokemonApi"))

        retrofit.create(PokemonApi::class.java)
    }
}

@Single
class PokemonClient(private val api: PokemonApi) {
    fun getPokemonList(offset: Int? = null, limit: Int? = null) = api.getPokemonList(offset, limit)

    fun getPokemon(idOrName: String) = api.getPokemon(idOrName)

    fun getPokemonSpecies(idOrName: String) = api.getPokemonSpecies(idOrName)
}
