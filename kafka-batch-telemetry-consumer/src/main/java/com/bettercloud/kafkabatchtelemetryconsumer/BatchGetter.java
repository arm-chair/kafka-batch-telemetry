package com.bettercloud.kafkabatchtelemetryconsumer;


import io.opentelemetry.context.propagation.TextMapGetter;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class BatchGetter implements TextMapGetter<Headers> {

    public static final com.bettercloud.kafkabatchtelemetryconsumer.BatchGetter GETTER = new com.bettercloud.kafkabatchtelemetryconsumer.BatchGetter();

    @Override
    public Iterable<String> keys(Headers headers) {
        return StreamSupport.stream(headers.spliterator(), false)
                .map(Header::key)
                .collect(Collectors.toList());
    }

    @Override
    public String get(Headers headers, String key) {
        Header header = headers.lastHeader(key);
        if (header == null) {
            return null;
        }
        byte[] value = header.value();
        if (value == null) {
            return null;
        }
        return new String(value, StandardCharsets.UTF_8);
    }
}
