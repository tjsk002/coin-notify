package com.coin_notify.coin_notify.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table("like_markets")
public class LikeMarketEntity extends BasicEntity {
	@Id
	private Long id;

	@Column("user_id")
	private Long userId;

	@Column("market_id")
	private Long marketId;

	@Column("is_active")
	private Boolean isActive;
}
