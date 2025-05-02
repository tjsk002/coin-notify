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

            return likeMarketRepository.findByUserIdAndMarketId(userId, marketId)
                    .flatMap(existingLike -> {
                        boolean newState = !Boolean.TRUE.equals(
                                existingLike.getIsActive());
                        existingLike.setIsActive(newState);

                        String message = newState ? "Like Added" : "Like Removed";
                        String data = Boolean.toString(newState);

                        return likeMarketRepository.save(existingLike).flatMap(savedEntity -> {
                            ApiResponse<String> response = new ApiResponse<>("A200", message, data);
                            return Mono.just(ResponseEntity.status(HttpStatus.OK).body(response));
                        });
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        LikeMarketEntity likeMarketEntity = new LikeMarketEntity();
                        likeMarketEntity.setUserId(userId);
                        likeMarketEntity.setMarketId(marketId);
                        likeMarketEntity.setIsActive(true);

                        return likeMarketRepository.save(likeMarketEntity).flatMap(savedEntity -> {
                            ApiResponse<String> response = new ApiResponse<>("A200", "Like Added",
                                    "true");
                            return Mono.just(ResponseEntity.status(HttpStatus.OK).body(response));
                        });
                    }));
        }).switchIfEmpty(Mono.just(ErrorMessage.USER_NOT_FOUND.toResponseEntity()));
    }
}
