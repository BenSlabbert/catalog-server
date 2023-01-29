package com.fluent.item.service;

import com.fluent.item.web.dto.ItemDto;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.support.BoundedAsyncPool;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.reactive.TransactionalOperator;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncReplicator {

  private final BoundedAsyncPool<StatefulRedisConnection<String, String>> boundedAsyncPool;
  private final ReactiveTransactionManager transactionManager;
  private final ItemService itemService;

  public void replicate(ItemDto dto) {
    upsert(dto)
        .whenComplete(
            (v, t) -> {
              if (t == null) {
                TransactionalOperator rxtx = TransactionalOperator.create(transactionManager);
                itemService.markAsReplicated(dto.id()).as(rxtx::transactional).subscribe();
              }
            });
  }

  public void delete(Long id) {
    boundedAsyncPool
        .acquire()
        .thenCompose(
            conn ->
                conn.async()
                    .del("item:" + id)
                    .whenComplete((s, t) -> boundedAsyncPool.release(conn)))
        .whenComplete(
            (s, t) -> {
              if (t == null) {
                TransactionalOperator rxtx = TransactionalOperator.create(transactionManager);
                itemService.delete(id).as(rxtx::transactional).subscribe();
              }
            });
  }

  private CompletableFuture<Void> upsert(ItemDto value) {
    return boundedAsyncPool
        .acquire()
        .thenCompose(
            conn -> {
              // leave as example of pipelining
              conn.setAutoFlushCommands(false);
              RedisAsyncCommands<String, String> async = conn.async();

              List<CompletableFuture<?>> futures =
                  List.of(async.hset("item:" + value.id(), value.asMap()).toCompletableFuture());

              conn.flushCommands();

              return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                  .whenComplete(
                      (s, t) -> {
                        // need to reset this
                        conn.setAutoFlushCommands(true);
                        boundedAsyncPool.release(conn);
                      });
            });
  }
}
