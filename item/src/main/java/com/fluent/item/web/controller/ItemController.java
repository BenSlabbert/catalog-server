package com.fluent.item.web.controller;

import com.fluent.item.service.AsyncReplicator;
import com.fluent.item.service.ItemService;
import com.fluent.item.web.dto.CreateItemDto;
import com.fluent.item.web.dto.ItemDto;
import com.fluent.item.web.dto.UpdateItemDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/item")
@RequiredArgsConstructor
public class ItemController {

  private final ThreadPoolTaskExecutor executor;
  private final AsyncReplicator publisher;
  private final ItemService itemService;

  @GetMapping("/all")
  public Flux<ItemDto> findAll() {
    return itemService.findAll();
  }

  @GetMapping("/search")
  public Mono<List<ItemDto>> search(
      @RequestParam("s") String term,
      @RequestParam(value = "limit", required = false, defaultValue = "10") long limit) {
    if (term.length() < 2) {
      return Mono.error(new IllegalAccessException("search term must longer than 2 chars"));
    }
    return itemService.search(term, limit);
  }

  @GetMapping("/{id}")
  public Mono<ItemDto> getById(@PathVariable Long id) {
    return itemService.getById(id);
  }

  @PostMapping
  public Mono<ItemDto> create(@RequestBody CreateItemDto createItemDto) {
    return itemService
        .create(createItemDto)
        .doOnSuccess(dto -> executor.submit(() -> publisher.replicate(dto)));
  }

  @PostMapping("/{id}")
  public Mono<ItemDto> update(@PathVariable Long id, @RequestBody UpdateItemDto updateItemDto) {
    return itemService
        .update(id, updateItemDto)
        .doOnSuccess(dto -> executor.submit(() -> publisher.replicate(dto)));
  }

  @DeleteMapping("/{id}")
  public Mono<Void> delete(@PathVariable Long id) {
    return itemService.markAsDeleted(id).doOnSuccess(v -> publisher.delete(id));
  }
}
