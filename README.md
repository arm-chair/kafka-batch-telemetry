This repository includes setup to show issues with Spring Batch Kafka and Tracing.

1. Spin up local Lightstep satellite 
1. Make sure kafka is running on default port (9092)
1. Run kafka-batch-telemetry-consumer with agent
   ```
   cd kafka-batch-telemetry-consumer
   export LS_SERVICE_NAME=consumer
   export OTEL_EXPORTER_OTLP_SPAN_INSECURE=true
   export OTEL_EXPORTER_OTLP_SPAN_ENDPOINT=localhost:8360
   ./gradlew bootJar
    java -javaagent:../lightstep-opentelemetry-javaagent.jar -jar build/libs/kafka-batch-telemetry-consumer-0.0.1-SNAPSHOT.jar
   ```
1. Run kafka-batch-telemetry-producer with agent
      ```
   cd kafka-batch-telemetry-producer
   export LS_SERVICE_NAME=producer
   export OTEL_EXPORTER_OTLP_SPAN_INSECURE=true
   export OTEL_EXPORTER_OTLP_SPAN_ENDPOINT=localhost:8360
   ./gradlew bootJar
    java -javaagent:../lightstep-opentelemetry-javaagent.jar -jar build/libs/kafka-batch-telemetry-producer-0.0.1-SNAPSHOT.jar
   ```
1. Send message to single endpoint `PUT http://localhost:8080/single/{message}`
1. See the complete trace from `kafka-batch-telemetry-producer` to `kafka-batch-telemetry-consumer`
1. Send message to batch endpoint `PUT http://localhost:8080/batch/{message}`
1. See that each message sent from `kafka-batch-telemetry-consumer` generates a new trace for each message sent