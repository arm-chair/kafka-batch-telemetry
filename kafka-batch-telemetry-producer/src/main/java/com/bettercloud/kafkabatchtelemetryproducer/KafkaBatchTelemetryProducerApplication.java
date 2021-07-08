package com.bettercloud.kafkabatchtelemetryproducer;

import io.opentelemetry.api.trace.Span;
import java.util.concurrent.ExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class KafkaBatchTelemetryProducerApplication {

	@RestController
	public class SendMessage {
		@Autowired
		private final KafkaTemplate<String, String> kafkaTemplate;

		public SendMessage(KafkaTemplate<String, String> kafkaTemplate) {
			this.kafkaTemplate = kafkaTemplate;
		}

		@PutMapping("/single/{message}")
		public void sendToSingle(@PathVariable String message) {

			for (int i = 0; i < 100; i++) {
				kafkaTemplate.send("single-topic", message.repeat(100) + i);
			}
		}

		@PutMapping("/batch/{message}")
		public void sendToBatch(@PathVariable String message) {
			for (int i = 0; i < 100; i++) {
				kafkaTemplate.send("batch-topic", message.repeat(100) + i);
			}
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(KafkaBatchTelemetryProducerApplication.class, args);
	}

}
