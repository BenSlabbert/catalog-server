package com.fluent.item.web.dto;

import java.util.Map;

public record ItemDto(Long id, String name) {

  public Map<String, String> asMap() {
    return Map.of("id", Long.toString(id), "name", name);
  }
}
