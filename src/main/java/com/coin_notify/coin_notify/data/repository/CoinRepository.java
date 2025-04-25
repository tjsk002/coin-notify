package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.CoinEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoinRepository extends ReactiveCrudRepository<CoinEntity, Long> {
	@Query("SELECT market FROM coins")
	Flux<String> findAllMarketNames();
	Mono<CoinEntity> findByMarket(String market);
}