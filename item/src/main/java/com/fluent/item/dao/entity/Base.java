package com.fluent.item.dao.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public abstract class Base {

  @Id private Long id;

  private boolean replicated;

  private boolean deleted;
}
