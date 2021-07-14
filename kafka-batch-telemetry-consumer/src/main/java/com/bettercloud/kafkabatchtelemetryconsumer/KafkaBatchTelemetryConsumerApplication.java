package com.bettercloud.kafkabatchtelemetryconsumer;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.bettercloud.kafkabatchtelemetryconsumer.BatchGetter.GETTER;
// import static io.opentelemetry.javaagent.instrumentation.kafkaclients.TextMapExtractAdapter.GETTER;

@SpringBootApplication
public class KafkaBatchTelemetryConsumerApplication {

    @Service
    public class SpanListener {

        private final KafkaTemplate<String, String> kafkaTemplate;

        public SpanListener(KafkaTemplate<String, String> kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

        @KafkaListener(topics = "single-topic", groupId = "single-group")
        public void onMessage(String message) {
            System.out.println(message);

            kafkaTemplate.send("final-topic", message);
        }

        @KafkaListener(topics = "batch-topic", groupId = "batch-group", containerFactory = "batchFactory")
        public void onBatchMessage(List<ConsumerRecord<String, String>> messages) {
            System.out.println(messages.size());
            messages.forEach(record -> {
                String message = record.value();
                System.out.println(message);
                Context extractedContext = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
                        .extract(Context.current(), record.headers(), GETTER);
                try (Scope ignored = extractedContext.makeCurrent()) {
                    kafkaTemplate.send("final-topic", message);
                }
            });
        }
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> batchFactory(
            ConsumerFactory<String, String> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);
        return factory;
    }

    public static void main(String[] args) {
        SpringApplication.run(KafkaBatchTelemetryConsumerApplication.class, args);
    }

}
