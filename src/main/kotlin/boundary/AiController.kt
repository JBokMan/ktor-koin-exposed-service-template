package com.example.boundary

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequest
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
import ai.koog.prompt.message.Message
import ai.koog.prompt.structure.json.JsonSchemaGenerator
import ai.koog.prompt.structure.json.JsonStructuredData
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.annotations.ResponseEntry
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single
class AiController {
    private val log = KotlinLogging.logger { this::class::simpleName }

    @GenerateOpenApi
    fun registerAiRoutes(application: Application) {
        application.routing {
            @KtorResponds(
                mapping =
                    [
                        ResponseEntry(
                            status = "201",
                            type = Int::class,
                            description =
                                "The Prompt was sent successfully, and the result is returned",
                        ),
                        ResponseEntry(
                            status = "400",
                            type = Unit::class,
                            description = "The request was malformed",
                        ),
                    ]
            )
            @KtorDescription(
                summary = "Send a prompt to the AI service",
                description = "Send a prompt to the AI service and receive a response",
            )
            post("/koog/ai/prompt") {
                val aiRequest = call.receive<AiRequest>()
                log.info { "Got ai request $aiRequest" }

                val apiKey = aiRequest.apiKey
                val systemPrompt = aiRequest.systemPrompt

                // Create sample forecasts
                val exampleForecasts =
                    listOf(
                        SimpleWeatherForecast(
                            location = "New York",
                            temperature = 25,
                            conditions = "Sunny",
                        ),
                        SimpleWeatherForecast(
                            location = "London",
                            temperature = 18,
                            conditions = "Cloudy",
                        ),
                    )

                // Generate JSON Schema
                val forecastStructure =
                    JsonStructuredData.createJsonStructure<SimpleWeatherForecast>(
                        schemaFormat = JsonSchemaGenerator.SchemaFormat.JsonSchema,
                        examples = exampleForecasts,
                        schemaType = JsonStructuredData.JsonSchemaType.SIMPLE,
                    )

                // Define the agent strategy
                val agentStrategy =
                    strategy("weather-forecast") {
                        val setup by nodeLLMRequest()

                        val getStructuredForecast by
                            node<Message.Response, String> { _ ->
                                val structuredResponse =
                                    llm.writeSession {
                                        this.requestLLMStructured(
                                            structure = forecastStructure,
                                            fixingModel = OpenAIModels.Chat.GPT4o,
                                        )
                                    }

                                """
                            Response structure:
                            $structuredResponse
                        """
                                    .trimIndent()
                            }

                        edge(nodeStart forwardTo setup)
                        edge(setup forwardTo getStructuredForecast)
                        edge(getStructuredForecast forwardTo nodeFinish)
                    }

                // Configure and run the agent
                val agentConfig =
                    AIAgentConfig(
                        prompt =
                            prompt("weather-forecast-prompt") {
                                system(
                                    content =
                                        """
                                    You are a weather forecasting assistant.
                                    When asked for a weather forecast, provide a realistic but fictional forecast.
                                """
                                            .trimIndent()
                                )
                            },
                        model = OpenAIModels.Chat.GPT4o,
                        maxAgentIterations = 5,
                    )

                val agent =
                    AIAgent(
                        promptExecutor = simpleOpenAIExecutor(apiKey),
                        toolRegistry = ToolRegistry.EMPTY,
                        strategy = agentStrategy,
                        agentConfig = agentConfig,
                    )

                return@post try {
                    val result = agent.run("Get weather forecast for Paris")
                    log.info { result }
                    call.respond(status = HttpStatusCode.Created, message = result)
                } catch (e: Exception) {
                    log.error(e) { "Failed to process AI request" }
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = e.message ?: "Internal Server Error",
                    )
                }
            }
        }
    }
}

@Serializable
data class AiRequest(val systemPrompt: String, val userPrompt: String, val apiKey: String)

// Note: Import statements are omitted for brevity
@Serializable
@SerialName("SimpleWeatherForecast")
@LLMDescription("Simple weather forecast for a location")
data class SimpleWeatherForecast(
    @property:LLMDescription("Location name") val location: String,
    @property:LLMDescription("Temperature in Celsius") val temperature: Int,
    @property:LLMDescription("Weather conditions (e.g., sunny, cloudy, rainy)")
    val conditions: String,
)
