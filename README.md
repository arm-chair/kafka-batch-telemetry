This repository includes setup to show issues with Spring Batch Kafka and Tracing.

1. Make sure kafka is running on default port (9092)
2. Run kafka-batch-telemetry-consumer with agent
3. Run kafka-batch-telemetry-producer with agent
4. Send message to single endpoint `PUT http://localhost:8080/single/{message}`
5. See the complete trace from `kafka-batch-telemetry-producer` to `kafka-batch-telemetry-consumer`
6. Send message to batch endpoint `PUT http://localhost:8080/batch/{message}`
7. See that each message sent from `kafka-batch-telemetry-consumer` generates a new trace for each message sent