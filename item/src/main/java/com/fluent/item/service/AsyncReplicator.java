package com.fluent.item.service;

import com.fluent.item.config.RedisConfig;
import com.fluent.item.web.dto.ItemDto;
import com.fluent.item.web.dto.ItemDtoJsonWriter;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncReplicator {

  private final BoundedAsyncPool<StatefulRedisModulesConnection<String, String>> boundedAsyncPool;
  private final ItemService itemService;

  @EventListener
  public void startup(ApplicationReadyEvent e) {
    // https://redis.io/docs/stack/search/indexing_json/#create-index-with-json-schema
    // FT.CREATE itemIdx ON JSON PREFIX 1 item: SCHEMA $.name AS name TEXT

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
                        Field.numeric("$." + ItemDtoJsonWriter.ID).as(ItemDtoJsonWriter.ID).build(),
                        Field.text("$." + ItemDtoJsonWriter.NAME)
                            .as(ItemDtoJsonWriter.NAME)
                            .withSuffixTrie()
                            .build())
                    .whenComplete((s, t) -> boundedAsyncPool.release(conn)));

    // find all items not yet replicated and replicate them
    itemService
        .findAllNotReplicated()
        .buffer(128)
        .subscribe(dtos -> upsertMany(dtos).whenComplete(markAllAsReplicated(dtos)));
  }

  public void replicate(ItemDto dto) {
    log.info("replicating: {}", dto);
    upsert(dto).whenComplete(markAsReplicated(dto));
  }

  public void delete(Long id) {
    log.info("delete: {}", id);
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
                itemService.delete(id).subscribe();
              }
            });
  }

  private BiConsumer<Void, Throwable> markAsReplicated(ItemDto itemDto) {
    return (v, t) -> {
      if (t == null) {
        itemService.markAsReplicated(itemDto.id()).subscribe();
      }
    };
  }

  private BiConsumer<Void, Throwable> markAllAsReplicated(List<ItemDto> dtos) {
    return (v, t) -> {
      if (t == null) {
        List<Long> ids = dtos.stream().map(ItemDto::id).toList();
        itemService.markAllAsReplicated(ids).subscribe();
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
                                  .jsonSet(
                                      itemKey(value.id()),
                                      "$",
                                      ItemDtoJsonWriter.toJsonString(value))
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
                    .jsonSet(itemKey(value.id()), "$", ItemDtoJsonWriter.toJsonString(value))
                    .whenComplete((s, t) -> boundedAsyncPool.release(conn)))
        .thenAccept(l -> log.info("updated json: {}", l));
  }

  private String itemKey(long id) {
    return "item:" + id;
  }
}
