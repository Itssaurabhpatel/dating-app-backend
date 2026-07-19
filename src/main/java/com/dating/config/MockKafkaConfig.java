package com.dating.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.SendResult;
import java.util.concurrent.CompletableFuture;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import java.util.HashMap;

@Configuration
@RequiredArgsConstructor
public class MockKafkaConfig {

    private final ApplicationEventPublisher eventPublisher;

    @Bean
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        ProducerFactory<String, Object> pf = new DefaultKafkaProducerFactory<>(new HashMap<>());
        return new KafkaTemplate<String, Object>(pf) {
            @Override
            public CompletableFuture<SendResult<String, Object>> send(String topic, Object data) {
                eventPublisher.publishEvent(new LocalEvent(topic, data));
                return CompletableFuture.completedFuture(null);
            }

            @Override
            public CompletableFuture<SendResult<String, Object>> send(String topic, String key, Object data) {
                eventPublisher.publishEvent(new LocalEvent(topic, data));
                return CompletableFuture.completedFuture(null);
            }
        };
    }

    public static class LocalEvent {
        private final String topic;
        private final Object data;

        public LocalEvent(String topic, Object data) {
            this.topic = topic;
            this.data = data;
        }

        public String getTopic() { return topic; }
        public Object getData() { return data; }
    }
}
