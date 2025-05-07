package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.RealTimePriceEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface RealTimePriceRepository extends ReactiveCrudRepository<RealTimePriceEntity, Long> {
}
