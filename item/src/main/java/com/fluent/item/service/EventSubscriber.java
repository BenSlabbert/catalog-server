package com.fluent.item.service;

import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.reactive.ChannelMessage;
import io.lettuce.core.pubsub.api.reactive.RedisPubSubReactiveCommands;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class EventSubscriber {

  private final Flux<ChannelMessage<String, String>> channelMessageFlux;

  public EventSubscriber(
      GenericObjectPool<StatefulRedisPubSubConnection<String, String>> boundedAsyncPubSubPool)
      throws Exception {

    RedisPubSubReactiveCommands<String, String> reactive =
        boundedAsyncPubSubPool.borrowObject().reactive();
    reactive.subscribe("topic").subscribe();
    channelMessageFlux = reactive.observeChannels();
  }

  // leave as example for now...
  public Flux<ChannelMessage<String, String>> getFlux() {
    return channelMessageFlux;
  }
}
