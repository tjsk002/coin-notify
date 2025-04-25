package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.CoinEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CoinRepository extends ReactiveCrudRepository<CoinEntity, Long> {
	@Query("SELECT coins FROM coins")
	Flux<String> findAllNames();
	Mono<CoinEntity> findByName(String name);
	Mono<Boolean> existsByNameAndKoreanNameAndEnglishName(String Name, String koreanName, String englishName);
}