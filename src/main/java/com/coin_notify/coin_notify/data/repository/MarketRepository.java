package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.MarketEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MarketRepository extends ReactiveCrudRepository<MarketEntity, Long> {
	@Query("SELECT name FROM markets")
	Flux<String> findAllNames();
	Mono<MarketEntity> findByName(String name);
}