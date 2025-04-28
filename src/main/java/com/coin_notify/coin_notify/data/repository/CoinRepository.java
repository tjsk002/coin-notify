package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.CoinEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface CoinRepository extends ReactiveCrudRepository<CoinEntity, Long> {
	@Query("""
    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END
    FROM coins
    WHERE symbol = :symbol
    """)
	Mono<Boolean> existsBySymbol(String Symbol);
}