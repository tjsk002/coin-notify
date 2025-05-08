package com.coin_notify.coin_notify.data.repository;

import com.coin_notify.coin_notify.data.entity.NotificationEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface NotificationRepository extends ReactiveCrudRepository<NotificationEntity, Long> {
}
