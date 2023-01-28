package com.fluent.item.service;

import com.fluent.item.web.dto.ItemDto;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.BoundedAsyncPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AsyncPublisher {

  private final BoundedAsyncPool<StatefulRedisConnection<String, String>> boundedAsyncPool;

  public AsyncPublisher(
      BoundedAsyncPool<StatefulRedisConnection<String, String>> boundedAsyncPool) {
    this.boundedAsyncPool = boundedAsyncPool;
  }

  public void publish(ItemDto dto) {
    boundedAsyncPool
        .acquire()
        .thenCompose(
            conn ->
                conn.async()
                    .publish("topic", dto.id() + ":" + dto.name())
                    .whenComplete((s, t) -> boundedAsyncPool.release(conn)))
        .thenAccept(subscribers -> log.info("number of subscribers: {}", subscribers));
  }
}
