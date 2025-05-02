package com.coin_notify.coin_notify.service;

import com.coin_notify.coin_notify.data.entity.LikeCoinEntity;
import com.coin_notify.coin_notify.data.repository.LikeCoinRepository;
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
public class LikeCoinService {
    private final LikeCoinRepository likeCoinRepository;
    private final UserRepository userRepository;

    public LikeCoinService(LikeCoinRepository likeCoinRepository, UserRepository userRepository) {
        this.likeCoinRepository = likeCoinRepository;
        this.userRepository = userRepository;
    }

    public Mono<ResponseEntity<ApiResponse<String>>> addLikeCoin(HttpServletRequest request, Long coinId) {
        UUID userUuid = (UUID) request.getSession().getAttribute("user_uuid");
        if (userUuid == null) {
            return Mono.just(ErrorMessage.USER_UUID_NOT_FOUND.toResponseEntity());
        }

        return userRepository.findByUuid(userUuid).flatMap(user -> {
            Long userId = user.getId();

            return likeCoinRepository.findByUserIdAndCoinId(userId, coinId).flatMap(
                    existingLike -> {
                        existingLike.setIsActive(false);
                        return likeCoinRepository.save(existingLike).flatMap(savedEntity -> {
                            ApiResponse<String> response = new ApiResponse<>("A200", "Like Removed",
                                    "false");
                            return Mono.just((ResponseEntity.status(HttpStatus.OK).body(response)));
                        }).doOnError(throwable -> {
                            System.out.println("오류 " + throwable.getMessage());
                        });
                    }).switchIfEmpty(Mono.defer(() -> {
                LikeCoinEntity likeCoinEntity = new LikeCoinEntity();
                likeCoinEntity.setUserId(userId);
                likeCoinEntity.setCoinId(coinId);
                likeCoinEntity.setIsActive(true);

                return likeCoinRepository.save(likeCoinEntity).flatMap(savedEntity -> {
                    ApiResponse<String> response = new ApiResponse<>("A200", "Success", "true");
                    return Mono.just(ResponseEntity.status(HttpStatus.OK).body(response));
                }).doOnError(throwable -> {
                    System.out.println("오류 " + throwable.getMessage());
                });
            }));
        }).switchIfEmpty(Mono.just(ErrorMessage.USER_NOT_FOUND.toResponseEntity()));
    }
}
