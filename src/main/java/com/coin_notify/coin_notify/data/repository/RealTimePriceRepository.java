package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.RealTimePriceEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RealTimePriceRepository extends ReactiveCrudRepository<RealTimePriceEntity, Long> {
	@Query("SELECT * FROM real_time_prices WHERE market_id = :marketId ORDER BY created_at DESC LIMIT 2")
	Flux<RealTimePriceEntity> findRecentPrices(Long marketId);
}
