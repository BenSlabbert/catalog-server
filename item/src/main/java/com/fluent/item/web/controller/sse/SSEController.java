package com.fluent.item.web.controller.sse;

import com.fluent.item.service.EventSubscriber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/sse")
public class SSEController {

  private final EventSubscriber publisher;

  public SSEController(EventSubscriber publisher) {
    this.publisher = publisher;
  }

  @GetMapping
  public Flux<ServerSentEvent<String>> streamEvents() {
    return publisher
        .getFlux()
        .map(
            message ->
                ServerSentEvent.<String>builder()
                    .id(String.valueOf(message.getChannel()))
                    .event(message.getChannel())
                    .data("SSE - " + message.getMessage())
                    .build());
  }
}
