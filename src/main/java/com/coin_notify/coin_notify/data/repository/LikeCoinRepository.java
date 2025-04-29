package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.LikeCoinEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LikeCoinRepository extends ReactiveCrudRepository<LikeCoinEntity, Long> {
	Mono<LikeCoinEntity> findByUserIdAndCoinId(Long userId, Long coinId);
}
