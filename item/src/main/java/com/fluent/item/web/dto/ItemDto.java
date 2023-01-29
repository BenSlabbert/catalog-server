package com.fluent.item.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public record ItemDto(Long id, String name) {

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
