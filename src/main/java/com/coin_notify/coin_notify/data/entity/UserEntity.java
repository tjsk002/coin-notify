package com.coin_notify.coin_notify.data.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Table("users")
public class UserEntity extends BasicEntity {
	@Id
	private Long id;

	@Column("uuid")
	private UUID uuid;

	@Column("user_agent")
	private String userAgent;
}
