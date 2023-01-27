package com.fluent.item;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.config.EnableWebFlux;

@EnableWebFlux
@SpringBootApplication
public class ItemApplication {

  public static void main(String[] args) {
    SpringApplication.run(ItemApplication.class, args);
  }
}
