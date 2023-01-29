package com.fluent.item.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;

public record ItemDto(Long id, String name) {

  public static ItemDto fromSearch(Map<String, String> map) {
    return new ItemDto(Long.parseLong(map.get("id")), map.get("name"));
  }

  public String json() {
    // idea, maybe have a compile time code generator for this
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = mapper.createObjectNode();

    rootNode.put("id", id);
    rootNode.put("name", name);

    try {
      return mapper.writeValueAsString(rootNode);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
