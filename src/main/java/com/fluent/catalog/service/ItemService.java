package com.fluent.catalog.service;

import com.fluent.catalog.dao.entiy.Item;
import com.fluent.catalog.dao.repo.ItemRepo;
import com.fluent.catalog.mapper.ItemMapper;
import com.fluent.catalog.web.dto.CreateItemDto;
import com.fluent.catalog.web.dto.ItemDto;
import com.fluent.catalog.web.dto.UpdateItemDto;
import com.fluent.catalog.web.exception.NotFoundException;
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
}
