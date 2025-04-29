package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.LikeMarketEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface LikeMarketRepository extends ReactiveCrudRepository<LikeMarketEntity, Long> {
	Mono<LikeMarketEntity> findByUserIdAndMarketId(Long userId, Long marketId);
}
