package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.MarketEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MarketRepository extends ReactiveCrudRepository<MarketEntity, Long> {
	@Query("SELECT market_code FROM markets")
	Flux<String> findAllMarketCodes();
	Mono<MarketEntity> findByMarketCode(String marketCode);
}