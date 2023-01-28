package com.fluent.item.web.controller;

import com.fluent.item.service.AsyncPublisher;
import com.fluent.item.service.ItemService;
import com.fluent.item.web.dto.CreateItemDto;
import com.fluent.item.web.dto.ItemDto;
import com.fluent.item.web.dto.UpdateItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

  private final ItemService itemService;
  private final AsyncPublisher publisher;

  @GetMapping("/all")
  public Flux<ItemDto> findAll() {
    return itemService.findAll();
  }

  @GetMapping("/{id}")
  public Mono<ItemDto> getById(@PathVariable Long id) {
    return itemService.getById(id);
  }

  @PostMapping
  public Mono<ItemDto> create(@RequestBody CreateItemDto createItemDto) {
    return itemService.create(createItemDto).doOnSuccess(dto -> publisher.publish(dto));
  }

  @PostMapping("/{id}")
  public Mono<ItemDto> update(@PathVariable Long id, @RequestBody UpdateItemDto updateItemDto) {
    return itemService.update(id, updateItemDto);
  }

  @DeleteMapping("/{id}")
  public Mono<Void> delete(@PathVariable Long id) {
    return itemService.delete(id);
  }
}
