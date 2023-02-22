package com.fluent.item.service;

import com.fluent.item.config.RedisConfig;
import com.fluent.item.dao.entity.Item;
import com.fluent.item.dao.repo.ItemRepo;
import com.fluent.item.mapper.ItemMapper;
import com.fluent.item.web.dto.CreateItemDto;
import com.fluent.item.web.dto.ItemDto;
import com.fluent.item.web.dto.ItemDtoJsonWriter;
import com.fluent.item.web.dto.UpdateItemDto;
import com.fluent.item.web.exception.NotFoundException;
import com.redis.lettucemod.api.StatefulRedisModulesConnection;
import com.redis.lettucemod.search.Limit;
import com.redis.lettucemod.search.SearchOptions;
import io.lettuce.core.support.BoundedAsyncPool;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

  private static final ItemMapper ITEM_MAPPER = ItemMapper.INSTANCE;

  private final BoundedAsyncPool<StatefulRedisModulesConnection<String, String>> boundedAsyncPool;
  private final ItemRepo itemRepo;

  public Flux<ItemDto> findAll() {
    return itemRepo.findAll().map(ITEM_MAPPER::toDto);
  }

  public Mono<ItemDto> getById(Long id) {
    return itemRepo.findById(id).map(ITEM_MAPPER::toDto);
  }

  public Mono<ItemDto> create(CreateItemDto createItemDto) {
    log.info("creating: {}", createItemDto);
    return itemRepo.save(Item.builder().name(createItemDto.name()).build()).map(ITEM_MAPPER::toDto);
  }

  public Mono<ItemDto> update(Long id, UpdateItemDto updateItemDto) {
    log.info("id: {} updating: {}", id, updateItemDto);
    return itemRepo
        .update(id, updateItemDto.name())
        .switchIfEmpty(Mono.error(new NotFoundException("no item found for id: " + id)))
        .map(ITEM_MAPPER::toDto);
  }

  public Flux<ItemDto> findAllNotReplicated() {
    return itemRepo.findAllNotReplicated().map(ITEM_MAPPER::toDto);
  }

  public Mono<Void> markAsReplicated(Long id) {
    log.info("marking as replicated: {}", id);
    return itemRepo.markAsReplicated(id);
  }

  public Mono<Void> markAllAsReplicated(Collection<Long> ids) {
    log.info("marking as replicated: {}", ids);
    return itemRepo.markAllAsReplicated(ids);
  }

  public Mono<Void> markAsDeleted(Long id) {
    log.info("marking as deleted: {}", id);
    return itemRepo.markAsDeleted(id);
  }

  public Mono<Void> delete(Long id) {
    log.info("deleting: {}", id);
    return itemRepo.deleteById(id);
  }

  public Mono<List<ItemDto>> search(String term, long limit) {
    var future =
        boundedAsyncPool
            .acquire()
            .thenCompose(
                conn ->
                    conn.async()
                        .ftSearch(
                            RedisConfig.ITEM_INDEX,
                            "@name:(*" + term + "*)",
                            SearchOptions.<String, String>builder()
                                .returnFields(ItemDtoJsonWriter.ID, ItemDtoJsonWriter.NAME)
                                .limit(Limit.offset(0L).num(limit))
                                .build())
                        .whenComplete((s, t) -> boundedAsyncPool.release(conn)))
            .thenApply(
                results ->
                    results.stream()
                        .map(
                            e ->
                                e.entrySet().stream()
                                    .collect(
                                        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                        .map(ItemDtoJsonWriter::fromMap)
                        .toList());

    return Mono.fromFuture(future);
  }
}
