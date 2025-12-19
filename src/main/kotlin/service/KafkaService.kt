package com.example.service

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer
import org.koin.core.annotation.Single

@Single
class KafkaService(appConfig: ApplicationConfig) {
    private val log = KotlinLogging.logger {}

    private val topic = appConfig.property("kafka.topic").getString()
    private val bootstrapServers = appConfig.property("kafka.bootstrapServers").getString()
    private val acksConfig = appConfig.property("kafka.acksConfig").getString()

    private val producerProps =
        mapOf(
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
            ProducerConfig.ACKS_CONFIG to acksConfig,
        )

    private val producer = KafkaProducer<String, String>(producerProps)

    suspend fun publishMessage(message: String, key: String = "default-key"): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val record = ProducerRecord(topic, key, message)
                producer.send(record).get()
                log.info { "Message published to Kafka: '$message'" }
                true
            } catch (e: Exception) {
                log.error(e) { "Failed to publish message to Kafka: '$message'" }
                false
            }
        }
    }

    fun close() {
        producer.close()
    }
}
