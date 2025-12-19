package com.example.boundary

import com.example.service.KafkaService
import io.github.oshai.kotlinlogging.KotlinLogging
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.routing
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Single
class KafkaController(private val kafkaService: KafkaService) {
    private val log = KotlinLogging.logger { this::class::simpleName }

    fun registerKafkaRoutes(application: Application) {
        application.routing {
            post(
                "/kafka/messages",
                {
                    summary = "Publish message to Kafka"
                    description = "Publish a message to the configured Kafka topic"
                    request { body<KafkaMessage>() }
                    response {
                        code(HttpStatusCode.Accepted) {
                            description = "The message was accepted for publishing to Kafka"
                        }
                        code(HttpStatusCode.BadRequest) {
                            description = "The request was malformed"
                        }
                        code(HttpStatusCode.InternalServerError) {
                            description = "Failed to publish the message to Kafka"
                        }
                    }
                },
            ) {
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
