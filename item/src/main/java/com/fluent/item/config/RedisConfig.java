package com.fluent.item.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.support.AsyncConnectionPoolSupport;
import io.lettuce.core.support.BoundedAsyncPool;
import io.lettuce.core.support.BoundedPoolConfig;
import io.lettuce.core.support.ConnectionPoolSupport;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

  @Bean
  RedisURI redisURI() {
    return RedisURI.builder().withHost("localhost").withPort(6379).withDatabase(0).build();
  }

  @Bean
  RedisClient redisClient() {
    return RedisClient.create();
  }

  @Bean
  BoundedAsyncPool<StatefulRedisConnection<String, String>> boundedAsyncPool(
      RedisClient redisClient, RedisURI redisURI) {
    return AsyncConnectionPoolSupport.createBoundedObjectPool(
        () -> redisClient.connectAsync(StringCodec.UTF8, redisURI), BoundedPoolConfig.create());
  }

  @Bean
  GenericObjectPool<StatefulRedisPubSubConnection<String, String>> boundedAsyncPubSubPool(
      RedisClient redisClient, RedisURI redisURI) {

    return ConnectionPoolSupport.createGenericObjectPool(
        () -> redisClient.connectPubSub(StringCodec.UTF8, redisURI),
        new GenericObjectPoolConfig<>());
  }
}
