package com.fluent.item.service;

import com.fluent.item.web.dto.ItemDto;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.support.BoundedAsyncPool;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
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

  @EventListener
  public void startup(ApplicationReadyEvent e) {
    // find all items not yet replicated and replicate them
    itemService
        .findAllNotReplicated()
        .buffer(5)
        .subscribe(dtos -> upsertMany(dtos).whenComplete(markAllAsReplicated(dtos)));
  }

  public void replicate(ItemDto dto) {
    upsert(dto).whenComplete(markAsReplicated(dto));
  }

  public void delete(Long id) {
    boundedAsyncPool
        .acquire()
        .thenCompose(
            conn ->
                conn.async()
                    .del(itemKey(id))
                    .whenComplete((s, t) -> boundedAsyncPool.release(conn)))
        .whenComplete(
            (s, t) -> {
              if (t == null) {
                TransactionalOperator rxtx = TransactionalOperator.create(transactionManager);
                itemService.delete(id).as(rxtx::transactional).subscribe();
              }
            });
  }

  private BiConsumer<Void, Throwable> markAsReplicated(ItemDto itemDto) {
    return (v, t) -> {
      if (t == null) {
        TransactionalOperator rxtx = TransactionalOperator.create(transactionManager);
        itemService.markAsReplicated(itemDto.id()).as(rxtx::transactional).subscribe();
      }
    };
  }

  private BiConsumer<Void, Throwable> markAllAsReplicated(List<ItemDto> dtos) {
    return (v, t) -> {
      if (t == null) {
        List<Long> ids = dtos.stream().map(ItemDto::id).toList();
        TransactionalOperator rxtx = TransactionalOperator.create(transactionManager);
        itemService.markAllAsReplicated(ids).as(rxtx::transactional).subscribe();
      }
    };
  }

  private CompletableFuture<Void> upsertMany(Collection<ItemDto> values) {
    return boundedAsyncPool
        .acquire()
        .thenCompose(
            conn -> {
              conn.setAutoFlushCommands(false);
              RedisAsyncCommands<String, String> async = conn.async();

              List<CompletableFuture<Long>> futures =
                  values.stream()
                      .map(
                          value ->
                              async.hset(itemKey(value.id()), value.asMap()).toCompletableFuture())
                      .toList();

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

  private CompletableFuture<Void> upsert(ItemDto value) {
    return boundedAsyncPool
        .acquire()
        .thenCompose(
            conn ->
                conn.async()
                    .hset(itemKey(value.id()), value.asMap())
                    .whenComplete((s, t) -> boundedAsyncPool.release(conn)))
        .thenAccept(l -> log.info("updated hsets: {}", l));
  }

  private String itemKey(long id) {
    return "item:" + id;
  }
}
