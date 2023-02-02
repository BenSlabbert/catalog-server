package com.fluent.item.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisConfigProperties {

  private String host;
  private Integer port;
  private Integer db;
}
