package com.coin_notify.coin_notify.service;

import com.coin_notify.coin_notify.data.entity.UserEntity;
import com.coin_notify.coin_notify.data.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserService {
	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public Mono<UUID> setupUserSession(HttpServletRequest request) {
		UUID userUuid = (UUID)request.getSession().getAttribute("user_uuid");
		String userAgent = request.getHeader("User-Agent");

		if (userUuid == null) {
			userUuid = UUID.randomUUID();
			request.getSession().setAttribute("user_uuid", userUuid);
			saveUser(userUuid, userAgent).block();
		}

		return Mono.just(userUuid);
	}

	private Mono<UserEntity> saveUser(UUID uuid, String userAgent) {
		UserEntity userEntity = new UserEntity();
		userEntity.setUuid(uuid);
		userEntity.setUserAgent(userAgent);
		return userRepository.save(userEntity);
	}
}
