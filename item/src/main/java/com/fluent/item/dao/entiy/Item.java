package com.fluent.item.dao.entiy;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class Item {

  @Id private Long id;

  private String name;

  @Builder
  public Item(String name) {
    this.name = name;
  }
}
