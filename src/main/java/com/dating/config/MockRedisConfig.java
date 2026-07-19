package com.dating.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Configuration
public class MockRedisConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return new RedisConnectionFactory() {
            @Override public org.springframework.data.redis.connection.RedisConnection getConnection() { return null; }
            @Override public org.springframework.data.redis.connection.RedisClusterConnection getClusterConnection() { return null; }
            @Override public boolean getConvertPipelineAndTxResults() { return false; }
            @Override public org.springframework.data.redis.connection.RedisSentinelConnection getSentinelConnection() { return null; }
            @Override public org.springframework.dao.DataAccessException translateExceptionIfPossible(RuntimeException ex) { return null; }
        };
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        return new MockStringRedisTemplate(connectionFactory);
    }

    private static class MockStringRedisTemplate extends StringRedisTemplate {
        private final Map<String, String> storage = new ConcurrentHashMap<>();

        public MockStringRedisTemplate(RedisConnectionFactory connectionFactory) {
            super(connectionFactory);
        }

        @Override
        public Boolean hasKey(String key) {
            return storage.containsKey(key);
        }

        @Override
        public Boolean delete(String key) {
            return storage.remove(key) != null;
        }

        @Override
        public Boolean expire(String key, long timeout, TimeUnit unit) {
            return true;
        }

        @Override
        @SuppressWarnings("unchecked")
        public ValueOperations<String, String> opsForValue() {
            return (ValueOperations<String, String>) Proxy.newProxyInstance(
                ValueOperations.class.getClassLoader(),
                new Class<?>[]{ValueOperations.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        String methodName = method.getName();
                        if ("get".equals(methodName)) {
                            return storage.get(args[0]);
                        } else if ("set".equals(methodName)) {
                            storage.put((String) args[0], (String) args[1]);
                            return null;
                        } else if ("increment".equals(methodName)) {
                            String val = storage.getOrDefault(args[0], "0");
                            long next = Long.parseLong(val) + 1;
                            storage.put((String) args[0], String.valueOf(next));
                            return next;
                        } else if ("decrement".equals(methodName)) {
                            String val = storage.getOrDefault(args[0], "0");
                            long next = Long.parseLong(val) - 1;
                            storage.put((String) args[0], String.valueOf(next));
                            return next;
                        } else if ("setIfAbsent".equals(methodName)) {
                            return storage.putIfAbsent((String) args[0], (String) args[1]) == null;
                        }
                        return null;
                    }
                }
            );
        }
    }
}
