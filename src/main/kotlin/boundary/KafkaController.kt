package com.example.boundary

import com.example.service.KafkaService
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
class KafkaController(private val kafkaService: KafkaService) {
    private val log = KotlinLogging.logger { this::class::simpleName }

    @GenerateOpenApi
    fun registerKafkaRoutes(application: Application) {
        application.routing {
            @KtorResponds(
                mapping =
                    [
                        ResponseEntry(
                            status = "202",
                            type = Unit::class,
                            description = "The message was accepted for publishing to Kafka",
                        ),
                        ResponseEntry(
                            status = "400",
                            type = Unit::class,
                            description = "The request was malformed",
                        ),
                        ResponseEntry(
                            status = "500",
                            type = Unit::class,
                            description = "Failed to publish the message to Kafka",
                        ),
                    ]
            )
            @KtorDescription(
                summary = "Publish message to Kafka",
                description = "Publish a message to the configured Kafka topic",
            )
            post("/kafka/messages") {
                try {
                    val kafkaMessage = call.receive<KafkaMessage>()
                    log.debug { "Publishing message to Kafka: ${kafkaMessage.message}" }

                    val success =
                        kafkaService.publishMessage(kafkaMessage.message, kafkaMessage.key)

                    if (success) {
                        call.respond(HttpStatusCode.Accepted)
                    } else {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            "Failed to publish message to Kafka",
                        )
                    }
                } catch (e: Exception) {
                    log.error(e) { "Error processing Kafka message request" }
                    call.respond(HttpStatusCode.BadRequest, "Invalid request format")
                }
            }
        }
    }
}

@Serializable data class KafkaMessage(val message: String, val key: String = "default-key")
