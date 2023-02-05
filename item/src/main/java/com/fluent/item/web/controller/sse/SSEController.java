package com.fluent.item.web.controller.sse;

import java.time.Duration;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/item/sse")
public class SSEController {

  private static final String EventType = "StreamEvent";

  record Event(int value) {}

  @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
  public Flux<ServerSentEvent<Event>> streamEvents() {
    // todo regularly check the session, if expired, throw unauthorized
    return Flux.fromStream(IntStream.range(0, 10).boxed())
        .delayElements(Duration.ofSeconds(1L))
        .map(
            integer ->
                ServerSentEvent.<Event>builder()
                    .id(Integer.toString(integer))
                    .event(EventType)
                    .data(new Event(integer))
                    .build())
        .doOnRequest(req -> log.info("requesting: {}", req))
        .doOnSubscribe(subscription -> log.info("subscription: {}", subscription))
        .doOnCancel(() -> log.info("cancelled"))
        .doOnComplete(() -> log.info("complete"));
  }
}
