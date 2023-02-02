package com.fluent.item.config;

import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import io.lettuce.core.RedisURI;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.support.AsyncConnectionPoolSupport;
import io.lettuce.core.support.BoundedAsyncPool;
import io.lettuce.core.support.BoundedPoolConfig;
import java.util.concurrent.CompletableFuture;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

  public static final String ITEM_INDEX = "item_index";

  @Bean
  RedisURI redisURI(RedisConfigProperties redisConfigProperties) {
    return RedisURI.builder()
        .withHost(redisConfigProperties.getHost())
        .withPort(redisConfigProperties.getPort())
        .withDatabase(redisConfigProperties.getDb())
        .build();
  }

  @Bean
  RedisModulesClient redisClient() {
    return RedisModulesClient.create();
  }

  @Bean
  BoundedAsyncPool<StatefulRedisModulesConnection<String, String>> boundedAsyncPool(
      RedisModulesClient redisClient, RedisURI redisURI) {

    return AsyncConnectionPoolSupport.createBoundedObjectPool(
        () -> CompletableFuture.supplyAsync(() -> redisClient.connect(StringCodec.UTF8, redisURI)),
        BoundedPoolConfig.create(),
        false);
  }
}
