package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.UserEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<UserEntity, Long> {
	Mono<UserEntity> findByUuid(UUID uuid);
}
