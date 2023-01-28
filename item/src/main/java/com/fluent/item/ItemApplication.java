package com.fluent.item;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.support.BoundedAsyncPool;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.config.EnableWebFlux;

@Slf4j
@EnableWebFlux
@SpringBootApplication
@EnableTransactionManagement
public class ItemApplication {

  private final RedisClient redisClient;
  private final BoundedAsyncPool<StatefulRedisConnection<String, String>> boundedAsyncPool;
  private final GenericObjectPool<StatefulRedisPubSubConnection<String, String>>
      boundedAsyncPubSubPool;

  public ItemApplication(
      RedisClient redisClient,
      BoundedAsyncPool<StatefulRedisConnection<String, String>> boundedAsyncPool,
      GenericObjectPool<StatefulRedisPubSubConnection<String, String>> boundedAsyncPubSubPool) {
    this.redisClient = redisClient;
    this.boundedAsyncPool = boundedAsyncPool;
    this.boundedAsyncPubSubPool = boundedAsyncPubSubPool;
  }

  public static void main(String[] args) {
    SpringApplication.run(ItemApplication.class, args);
  }

  @PreDestroy
  void destroy() {
    log.info("shutting down redis 1/3");
    boundedAsyncPubSubPool.close();
    log.info("shutting down redis 2/3");
    boundedAsyncPool.close();
    log.info("shutting down redis 3/3");
    redisClient.close();
    log.info("shutting down redis...done");
  }
}
