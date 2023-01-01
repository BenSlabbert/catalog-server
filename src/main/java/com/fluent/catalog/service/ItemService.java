package com.fluent.catalog.service;

import com.fluent.catalog.dao.entiy.Item;
import com.fluent.catalog.dao.repo.ItemRepo;
import com.fluent.catalog.mapper.ItemMapper;
import com.fluent.catalog.web.dto.CreateItemDto;
import com.fluent.catalog.web.dto.ItemDto;
import com.fluent.catalog.web.dto.UpdateItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
    return itemRepo.save(Item.builder().name(createItemDto.name()).build()).map(ITEM_MAPPER::toDto);
  }

  public Mono<ItemDto> update(Long id, UpdateItemDto updateItemDto) {
    return itemRepo
        .update(id, updateItemDto.name())
        .switchIfEmpty(Mono.error(new IllegalArgumentException("no item found for id: " + id)))
        .map(ITEM_MAPPER::toDto);
  }
}
