package com.example.config

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.Application
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.koin.core.annotation.Single
import reactor.core.Disposable
import reactor.kafka.receiver.KafkaReceiver
import reactor.kafka.receiver.ReceiverOptions

@Single
class KafkaConfiguration(
    appConfig: ApplicationConfig // Inject Ktor's configuration
) {
    private val log = KotlinLogging.logger {}

    private val topic = appConfig.property("kafka.topic").getString()
    private val bootstrapServers = appConfig.property("kafka.bootstrapServers").getString()
    private val groupId = appConfig.property("kafka.groupId").getString()
    private val autoOffsetReset = appConfig.property("kafka.autoOffsetReset").getString()

    private val properties =
        mapOf(
            ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG to bootstrapServers,
            ConsumerConfig.GROUP_ID_CONFIG to groupId,
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to autoOffsetReset,
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.ENABLE_METRICS_PUSH_CONFIG to false,
        )

    private var subscription: Disposable? = null

    fun configureKafka(application: Application) {
        // Start a coroutine tied to the Application's lifecycle
        application.launch(Dispatchers.IO) {
            val receiverOptions =
                ReceiverOptions.create<String, String>(properties).subscription(setOf(topic))

            val receiver = KafkaReceiver.create(receiverOptions)

            subscription =
                receiver
                    .receive()
                    .doOnNext { record ->
                        log.info { "Received Kafka message: '${record.value()}'" }
                        // Todo call your business logic here
                        record.receiverOffset().acknowledge()
                    }
                    .subscribe()
        }
    }

    fun close() {
        subscription?.dispose()
    }
}
