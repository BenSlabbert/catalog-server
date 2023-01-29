package com.fluent.item.service;

import com.fluent.item.config.RedisConfig;
import com.fluent.item.web.dto.ItemDto;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.api.async.RedisModulesAsyncCommands;
import com.redis.lettucemod.search.CreateOptions;
import com.redis.lettucemod.search.Field;
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

  private final BoundedAsyncPool<StatefulRedisModulesConnection<String, String>> boundedAsyncPool;
  private final ReactiveTransactionManager transactionManager;
  private final ItemService itemService;

  @EventListener
  public void startup(ApplicationReadyEvent e) {
    // ensure we have an index
    // todo fix this, not working
    // https://redis.io/docs/stack/search/indexing_json/#create-index-with-json-schema
    // FT.CREATE itemIdx ON JSON PREFIX 1 item: SCHEMA $.name AS name TEXT
    // above works

    boundedAsyncPool
        .acquire()
        .thenCompose(
            conn ->
                conn.async()
                    .ftCreate(
                        RedisConfig.ITEM_INDEX,
                        CreateOptions.<String, String>builder()
                            .on(CreateOptions.DataType.JSON)
                            .prefixes("1", "item:")
                            .build(),
                        Field.numeric("$.id").as("id").build(),
                        Field.text("$.name").as("name").withSuffixTrie().build())
                    .whenComplete((s, t) -> boundedAsyncPool.release(conn)));

    // find all items not yet replicated and replicate them
    itemService
        .findAllNotReplicated()
        .buffer(128)
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
              RedisModulesAsyncCommands<String, String> async = conn.async();

              List<CompletableFuture<String>> futures =
                  values.stream()
                      .map(
                          value ->
                              async
                                  .jsonSet(itemKey(value.id()), "$", value.json())
                                  .toCompletableFuture())
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
                    .jsonSet(itemKey(value.id()), "$", value.json())
                    .whenComplete((s, t) -> boundedAsyncPool.release(conn)))
        .thenAccept(l -> log.info("updated json: {}", l));
  }

  private String itemKey(long id) {
    return "item:" + id;
  }
}
