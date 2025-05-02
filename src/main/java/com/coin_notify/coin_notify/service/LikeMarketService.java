package com.coin_notify.coin_notify.service;

import com.coin_notify.coin_notify.data.entity.LikeMarketEntity;
import com.coin_notify.coin_notify.data.repository.LikeMarketRepository;
import com.coin_notify.coin_notify.data.repository.UserRepository;
import com.coin_notify.coin_notify.response.ApiResponse;
import com.coin_notify.coin_notify.response.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class LikeMarketService {
	private final LikeMarketRepository likeMarketRepository;
	private final UserRepository userRepository;

	public LikeMarketService(LikeMarketRepository likeMarketRepository, UserRepository userRepository) {
		this.likeMarketRepository = likeMarketRepository;
		this.userRepository = userRepository;
	}

	public Mono<ResponseEntity<ApiResponse<String>>> addLikeMarket(HttpServletRequest request, Long marketId) {
		UUID userUuid = (UUID) request.getSession().getAttribute("user_uuid");
		if (userUuid == null) {
			return Mono.just(ErrorMessage.USER_UUID_NOT_FOUND.toResponseEntity());
		}

		return userRepository.findByUuid(userUuid).flatMap(user -> {
			Long userId = user.getId();
			return likeMarketRepository.findByUserIdAndMarketId(userId, marketId).flatMap(
					existingLike -> Mono.just(ErrorMessage.ALREADY_LIKED.toResponseEntity())
			).switchIfEmpty(Mono.defer(() -> {
				LikeMarketEntity likeMarketEntity = new LikeMarketEntity();
				likeMarketEntity.setUserId(userId);
				likeMarketEntity.setMarketId(marketId);

				return likeMarketRepository.save(likeMarketEntity).flatMap(savedEntity -> {
					ApiResponse<String> response = new ApiResponse<>("A200", "Success", "ok");
					return Mono.just(ResponseEntity.status(HttpStatus.OK).body(response));
				}).doOnError(throwable -> {
					System.out.println("오류 " + throwable.getMessage());
				});
			}));
		}).switchIfEmpty(Mono.just(ErrorMessage.USER_NOT_FOUND.toResponseEntity()));
	}
}
