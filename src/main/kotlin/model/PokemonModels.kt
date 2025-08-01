package com.example.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PokemonListResponse(
    val count: Int,
    val next: String?,
    val previous: String?,
    val results: List<PokemonListItem>,
)

@Serializable data class PokemonListItem(val name: String, val url: String)

@Serializable
data class Pokemon(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val base_experience: Int,
    val abilities: List<PokemonAbility>,
    val types: List<PokemonType>,
    val stats: List<PokemonStat>,
    val sprites: PokemonSprites,
    val species: NamedApiResource,
)

@Serializable
data class PokemonAbility(
    val ability: NamedApiResource,
    @SerialName("is_hidden") val isHidden: Boolean,
    val slot: Int,
)

@Serializable data class PokemonType(val slot: Int, val type: NamedApiResource)

@Serializable
data class PokemonStat(
    val stat: NamedApiResource,
    @SerialName("base_stat") val baseStat: Int,
    val effort: Int,
)

@Serializable
data class PokemonSprites(
    @SerialName("front_default") val frontDefault: String?,
    @SerialName("front_shiny") val frontShiny: String?,
    @SerialName("front_female") val frontFemale: String?,
    @SerialName("front_shiny_female") val frontShinyFemale: String?,
    @SerialName("back_default") val backDefault: String?,
    @SerialName("back_shiny") val backShiny: String?,
    @SerialName("back_female") val backFemale: String?,
    @SerialName("back_shiny_female") val backShinyFemale: String?,
)

@Serializable data class NamedApiResource(val name: String, val url: String)

@Serializable
data class PokemonSpecies(
    val id: Int,
    val name: String,
    @SerialName("order") val order: Int,
    @SerialName("gender_rate") val genderRate: Int,
    @SerialName("capture_rate") val captureRate: Int,
    @SerialName("base_happiness") val baseHappiness: Int,
    @SerialName("is_baby") val isBaby: Boolean,
    @SerialName("is_legendary") val isLegendary: Boolean,
    @SerialName("is_mythical") val isMythical: Boolean,
    @SerialName("hatch_counter") val hatchCounter: Int,
    @SerialName("has_gender_differences") val hasGenderDifferences: Boolean,
    @SerialName("forms_switchable") val formsSwitchable: Boolean,
    @SerialName("growth_rate") val growthRate: NamedApiResource,
    @SerialName("color") val color: NamedApiResource,
    @SerialName("shape") val shape: NamedApiResource?,
    @SerialName("evolves_from_species") val evolvesFromSpecies: NamedApiResource?,
    @SerialName("habitat") val habitat: NamedApiResource?,
    @SerialName("generation") val generation: NamedApiResource,
    @SerialName("flavor_text_entries") val flavorTextEntries: List<FlavorTextEntry>,
)

@Serializable
data class FlavorTextEntry(
    @SerialName("flavor_text") val flavorText: String,
    val language: NamedApiResource,
    val version: NamedApiResource,
)
