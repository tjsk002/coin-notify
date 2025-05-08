package com.coin_notify.coin_notify.service;

import com.coin_notify.coin_notify.data.dto.CoinResponseDto;
import com.coin_notify.coin_notify.data.dto.LikeCoinDto;
import com.coin_notify.coin_notify.data.entity.CoinEntity;
import com.coin_notify.coin_notify.data.repository.CoinRepository;
import com.coin_notify.coin_notify.data.repository.LikeCoinRepository;
import com.coin_notify.coin_notify.data.repository.UserRepository;
import com.coin_notify.coin_notify.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CoinService {
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final HttpClient client;
    private final CoinRepository coinRepository;
    private final LikeCoinRepository likeCoinRepository;

    public CoinService(ObjectMapper objectMapper, UserRepository userRepository, CoinRepository coinRepository, LikeCoinRepository likeCoinRepository)
    {
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.coinRepository = coinRepository;
        this.client = HttpClient.newHttpClient();
        this.likeCoinRepository = likeCoinRepository;
    }

    public Mono<Void> coinCollectionProcess() {
        return fetchCoinData().flatMap(this::saveCoinData);
    }

    public Mono<ResponseEntity<ApiResponse<List<CoinResponseDto>>>> coinList(HttpServletRequest request) {
        UUID userUuid = (UUID) request.getSession().getAttribute("user_uuid");
        return coinRepository.findAll().collectList().flatMap(
                coins -> userRepository.findByUuid(userUuid).flatMap(user -> {
                    Long userId = user.getId();

                    List<Mono<CoinResponseDto>> coinDtoList = coins.stream().map(
                            coin -> likeCoinRepository.findByUserIdAndCoinId(userId,
                                    coin.getId()).map(userMarketLike -> {
                                Boolean isActive = userMarketLike.getIsActive();
                                LikeCoinDto likeCoinDto = new LikeCoinDto(isActive);
                                return new CoinResponseDto(coin, likeCoinDto);
                            }).defaultIfEmpty(new CoinResponseDto(coin, null))).collect(
                            Collectors.toList());

                    return Mono.zip(coinDtoList, results -> {
                        List<CoinResponseDto> coinDtos = Arrays.stream(results).map(
                                result -> (CoinResponseDto) result).collect(Collectors.toList());

                        ApiResponse<List<CoinResponseDto>> response = new ApiResponse<>("A200",
                                "Success", coinDtos);

                        return ResponseEntity.ok(response);
                    });
                }));
    }


    private Mono<JsonNode> fetchCoinData() {
        String url = "https://api.upbit.com/v1/market/all?is_details=true";

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("accept",
                "application/json").build();

        return Mono.fromCallable(() -> {
            try {
                HttpResponse<String> response = client.send(request,
                        HttpResponse.BodyHandlers.ofString());
                return objectMapper.readTree(response.body());
            } catch (Exception e) {
                throw new RuntimeException("Failed to fetch market data", e);
            }
        });
    }

    private Mono<Void> saveCoinData(JsonNode marketData) {
        return Flux.fromIterable(marketData).publishOn(Schedulers.boundedElastic()).flatMap(
                data -> {
                    String symbol = data.get("market").asText();
                    String koreanName = data.get("korean_name").asText();
                    String englishName = data.get("english_name").asText();
                    String coinSymbol = symbol.split("-")[1];

                    return coinRepository.existsBySymbol(coinSymbol).flatMap(exists -> {
                        if (Boolean.TRUE.equals(exists)) {
                            System.out.println(
                                    "Duplicate found, skipping: " + coinSymbol + " | " + koreanName + " | " + englishName);
                            return Mono.empty();
                        } else {
                            CoinEntity coinEntity = new CoinEntity();
                            coinEntity.setSymbol(coinSymbol);
                            coinEntity.setKoreanName(koreanName);
                            coinEntity.setEnglishName(englishName);

                            return coinRepository.save(coinEntity).doOnSuccess(
                                    saved -> System.out.println("Saved: " + saved.getSymbol()));
                        }
                    }).onErrorResume(error -> {
                        System.err.println("Error occurred: " + error.getMessage());
                        return Mono.empty();
                    });
                }).doOnError(
                error -> System.err.println("Error during saving: " + error.getMessage())).then();
    }
}
