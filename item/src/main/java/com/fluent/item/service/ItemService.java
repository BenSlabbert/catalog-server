package com.fluent.item.service;

import com.fluent.item.dao.entiy.Item;
import com.fluent.item.dao.repo.ItemRepo;
import com.fluent.item.mapper.ItemMapper;
import com.fluent.item.web.dto.CreateItemDto;
import com.fluent.item.web.dto.ItemDto;
import com.fluent.item.web.dto.UpdateItemDto;
import com.fluent.item.web.exception.NotFoundException;
import java.util.Collection;
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
}
