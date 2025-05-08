package com.coin_notify.coin_notify.service;

import com.coin_notify.coin_notify.data.dto.LikeMarketDto;
import com.coin_notify.coin_notify.data.dto.MarketResponseDto;
import com.coin_notify.coin_notify.data.entity.MarketEntity;
import com.coin_notify.coin_notify.data.repository.LikeMarketRepository;
import com.coin_notify.coin_notify.data.repository.MarketRepository;
import com.coin_notify.coin_notify.data.repository.UserRepository;
import com.coin_notify.coin_notify.response.ApiResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MarketService {
    private final ObjectMapper objectMapper;
    private final HttpClient client;
    private final MarketRepository marketRepository;
    private final LikeMarketRepository likeMarketRepository;
    private final UserRepository userRepository;

    public MarketService(
            ObjectMapper objectMapper,
            MarketRepository marketRepository,
            LikeMarketRepository likeMarketRepository, UserRepository userRepository)
    {
        this.objectMapper = objectMapper;
        this.marketRepository = marketRepository;
        this.client = HttpClient.newHttpClient();
        this.likeMarketRepository = likeMarketRepository;
        this.userRepository = userRepository;
    }

    public Mono<Void> marketCollectionProcess() {
        return fetchMarketData().flatMap(this::saveMarketData);
    }

    public Mono<ResponseEntity<ApiResponse<List<MarketResponseDto>>>> marketList(HttpServletRequest request) {
        UUID userUuid = (UUID) request.getSession().getAttribute("user_uuid");
        return marketRepository.findAll()
                .collectList()
                .flatMap(markets ->
                        userRepository.findByUuid(userUuid)
                                .flatMap(user -> {
                                    Long userId = user.getId();

                                    List<Mono<MarketResponseDto>> marketDtoList = markets.stream()
                                            .map(market ->
                                                    likeMarketRepository.findByUserIdAndMarketId(
                                                                    userId, market.getId())
                                                            .map(userMarketLike -> {
                                                                Boolean isActive = userMarketLike.getIsActive();
                                                                LikeMarketDto likeMarketDto = new LikeMarketDto(
                                                                        isActive);
                                                                return new MarketResponseDto(market,
                                                                        likeMarketDto);
                                                            })
                                                            .defaultIfEmpty(
                                                                    new MarketResponseDto(market,
                                                                            null))
                                            )
                                            .collect(Collectors.toList());

                                    return Mono.zip(marketDtoList, results -> {
                                        List<MarketResponseDto> marketDtos = Arrays.stream(results)
                                                .map(result -> (MarketResponseDto) result)
                                                .collect(Collectors.toList());

                                        ApiResponse<List<MarketResponseDto>> response = new ApiResponse<>(
                                                "A200",
                                                "Success",
                                                marketDtos
                                        );

                                        return ResponseEntity.ok(response);
                                    });
                                })
                );
    }

    private Mono<JsonNode> fetchMarketData() {
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

    private Mono<Void> saveMarketData(JsonNode marketData) {
        return Flux.fromIterable(marketData)
                .filter(data -> !data.get("market").asText().contains("LOOM"))
                .map(data -> {
                    MarketEntity marketEntity = new MarketEntity();
                    marketEntity.setMarketCode(data.get("market").asText());
                    marketEntity.setKoreanName(data.get("korean_name").asText());
                    marketEntity.setEnglishName(data.get("english_name").asText());
                    return marketEntity;
                })
                .flatMap(marketRepository::save)
                .doOnNext(saved -> System.out.println("Saved: " + saved.getMarketCode()))
                .doOnError(error -> System.err.println("Error saving: " + error.getMessage()))
                .then();
    }
}
