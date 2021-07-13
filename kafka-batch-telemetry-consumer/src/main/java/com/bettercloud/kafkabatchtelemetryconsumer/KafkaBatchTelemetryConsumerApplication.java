package com.bettercloud.kafkabatchtelemetryconsumer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

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
        TextMapGetter<ConsumerRecord<String, String>> getter =
                new TextMapGetter<>() {
                  @Override
                  public String get(ConsumerRecord<String, String> carrier, String key) {
                    Map<String, String> extracted = extractHeaders(carrier);
                    if (extracted.containsKey(key)) {
                      return extracted.get(key);
                    }
                    return null;
                  }

                  @Override
                  public Iterable<String> keys(ConsumerRecord<String, String> carrier) {
                    return StreamSupport.stream(carrier.headers().spliterator(), false).map(h -> h.key()).collect(Collectors.toList());
                  }
                };

        Context extractedContext = GlobalOpenTelemetry.getPropagators().getTextMapPropagator()
                .extract(Context.current(), record, getter);
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

  private Map<String, String> extractHeaders(ConsumerRecord record) {
    Map<String, String> headers = new HashMap<>();
    Arrays.stream(record.headers().toArray())
            .forEach(header -> {
              if (header.value() != null) {
                headers.put(header.key(), new String(header.value(), StandardCharsets.UTF_8));
              }
            });
    return headers;
  }

  public static void main(String[] args) {
    SpringApplication.run(KafkaBatchTelemetryConsumerApplication.class, args);
  }

}
