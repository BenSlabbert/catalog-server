package com.fluent.item.dao.entiy;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Item extends Base {

  private String name;

  @Builder
  public Item(String name) {
    this.name = name;
  }
}
