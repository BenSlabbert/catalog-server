package com.fluent.item;

import com.redis.lettucemod.RedisModulesClient;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import io.lettuce.core.support.BoundedAsyncPool;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.reactive.config.EnableWebFlux;

@Slf4j
@EnableWebFlux
@SpringBootApplication
@RequiredArgsConstructor
@EnableTransactionManagement
public class ItemApplication {

  private final BoundedAsyncPool<StatefulRedisModulesConnection<String, String>> boundedAsyncPool;
  private final ThreadPoolTaskExecutor executor;
  private final RedisModulesClient redisClient;

  public static void main(String[] args) {
    SpringApplication.run(ItemApplication.class, args);
  }

  @PreDestroy
  void destroy() {
    log.info("shutting down executor");
    executor.shutdown();
    log.info("shutting down redis 1/2");
    boundedAsyncPool.close();
    log.info("shutting down redis 2/2");
    redisClient.close();
    log.info("shutting down redis...done");
  }
}
