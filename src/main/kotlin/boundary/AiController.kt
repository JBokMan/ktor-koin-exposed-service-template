package com.example.boundary

import ai.koog.agents.core.agent.AIAgent
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.all.simpleOpenAIExecutor
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
                val userPrompt = aiRequest.userPrompt

                val agent =
                    AIAgent(
                        executor = simpleOpenAIExecutor(apiKey),
                        systemPrompt = systemPrompt,
                        llmModel = OpenAIModels.Chat.GPT4o,
                    )

                return@post try {
                    val result = agent.run(userPrompt)
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
