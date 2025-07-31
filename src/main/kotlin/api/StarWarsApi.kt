package com.example.api

import com.example.model.PeopleResponse
import com.example.model.PlanetResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StarWarsApi {
    @GET("people/") fun getPeople(@Query("page") page: Int? = null): Call<PeopleResponse>

    @GET("people/{id}/") fun getPerson(@Path("id") id: Int): Call<com.example.model.Person>

    @GET("planets/") fun getPlanets(@Query("page") page: Int? = null): Call<PlanetResponse>

    @GET("planets/{id}/") fun getPlanet(@Path("id") id: Int): Call<com.example.model.Planet>
}
