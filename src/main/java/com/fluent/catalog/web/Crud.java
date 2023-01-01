package com.fluent.catalog.web;

import com.fluent.catalog.service.ItemService;
import com.fluent.catalog.web.dto.CreateItemDto;
import com.fluent.catalog.web.dto.ItemDto;
import com.fluent.catalog.web.dto.UpdateItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/item")
@RequiredArgsConstructor
public class Crud {

  private final ItemService itemService;

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
    return itemService.create(createItemDto);
  }

  @PostMapping("/{id}")
  public Mono<ItemDto> update(@PathVariable Long id, @RequestBody UpdateItemDto updateItemDto) {
    return itemService.update(id, updateItemDto);
  }
}
