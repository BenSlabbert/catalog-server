package com.fluent.item.dao.repo;

import com.fluent.item.dao.entiy.Item;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ItemRepo extends ReactiveCrudRepository<Item, Long> {

  @Query("update item set name = :name where id = :id and deleted is false returning *")
  Mono<Item> update(Long id, String name);

  @Override
  @Query("select * from item where deleted is false order by id")
  Flux<Item> findAll();

  @Query("update item set replicated = true where id = :id")
  Mono<Void> markAsReplicated(long id);

  @Query("update item set deleted = true where id = :id")
  Mono<Void> markAsDeleted(long id);
}
