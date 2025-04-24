package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.CoinEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CoinRepository extends ReactiveCrudRepository<CoinEntity, Long> {
}