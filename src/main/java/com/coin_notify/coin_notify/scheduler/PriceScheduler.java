package com.coin_notify.coin_notify.scheduler;

import com.coin_notify.coin_notify.data.entity.NotificationEntity;
import com.coin_notify.coin_notify.data.entity.RealTimePriceEntity;
import com.coin_notify.coin_notify.data.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Component
public class PriceScheduler implements CommandLineRunner {
    private final UserRepository userRepository;
    private final MarketRepository marketRepository;
    private final LikeMarketRepository likeMarketRepository;
    private final RealTimePriceRepository realTimePriceRepository;
    private final NotificationRepository notificationRepository;
    private final ObjectMapper objectMapper;
    Map<UUID, Sinks.Many<ServerSentEvent<String>>> userSinkMap = new ConcurrentHashMap<>();

    public PriceScheduler(
            UserRepository userRepository,
            MarketRepository marketRepository,
            LikeMarketRepository likeMarketRepository,
            RealTimePriceRepository realTimePriceRepository,
            NotificationRepository notificationRepository,
            ObjectMapper objectMapper)
    {
        this.userRepository = userRepository;
        this.marketRepository = marketRepository;
        this.likeMarketRepository = likeMarketRepository;
        this.realTimePriceRepository = realTimePriceRepository;
        this.notificationRepository = notificationRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        fetchRealTimePrice();
    }

    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    public void fetchRealTimePrice() {
        marketRepository.findAllMarketCodes().collectList().filter(
                markets -> !markets.isEmpty()).map(markets -> String.join(",", markets)).flatMap(
                this::callUpbitApiWithMarkets).flatMapMany(response -> {
            try {
                JsonNode root = objectMapper.readTree(response);

                return Flux.fromIterable(root).flatMap(node -> {
                    String name = node.get("market").asText();
                    Double tradePrice = node.get("trade_price").asDouble();
                    String tradeDate = node.get("trade_date").asText();
                    String tradeTime = node.get("trade_time").asText();
                    String tradeDateKst = node.get("trade_date_kst").asText();
                    String tradeTimeKst = node.get("trade_time_kst").asText();
                    Long tradeTimestamp = node.get("trade_timestamp").asLong();
                    Double openingPrice = node.get("opening_price").asDouble();
                    Double highPrice = node.get("high_price").asDouble();
                    Double lowPrice = node.get("low_price").asDouble();
                    String change = node.get("change").asText();
                    Double changePrice = node.get("change_price").asDouble();
                    Double changeRate = node.get("change_rate").asDouble();
                    Double tradeVolume = node.get("trade_volume").asDouble();
                    Double accTradePrice = node.get("acc_trade_price").asDouble();
                    Double prevClosingPrice = node.get("prev_closing_price").asDouble();
                    Double accTradePrice24h = node.get("acc_trade_price_24h").asDouble();
                    Double accTradeVolume = node.get("acc_trade_volume").asDouble();
                    Double accTradeVolume24h = node.get("acc_trade_volume_24h").asDouble();
                    Long timestamp = node.get("timestamp").asLong();

                    return marketRepository.findByMarketCode(name).map(market -> {
                        RealTimePriceEntity price = new RealTimePriceEntity();
                        price.setMarketId(market.getId());
                        price.setTradePrice(tradePrice);
                        price.setTradeDate(tradeDate);
                        price.setTradeTime(tradeTime);
                        price.setTradeDateKst(tradeDateKst);
                        price.setTradeTimeKst(tradeTimeKst);
                        price.setTradeTimestamp(tradeTimestamp);
                        price.setOpeningPrice(openingPrice);
                        price.setHighPrice(highPrice);
                        price.setLowPrice(lowPrice);
                        price.setChange(change);
                        price.setChangePrice(changePrice);
                        price.setChangeRate(changeRate);
                        price.setTradeVolume(tradeVolume);
                        price.setAccTradePrice(accTradePrice);
                        price.setPrevClosingPrice(prevClosingPrice);
                        price.setAccTradePrice24h(accTradePrice24h);
                        price.setAccTradeVolume(accTradeVolume);
                        price.setAccTradeVolume24h(accTradeVolume24h);
                        price.setTimestamp(timestamp);
                        return price;
                    });
                });
            } catch (Exception e) {
                System.err.println("-- 오류 발생: " + e.getMessage());
                e.printStackTrace();
                return Flux.empty();
            }
        }).flatMap(realTimePriceRepository::save).then(checkLikeMarket()).subscribe(unused -> {
                }, error -> System.err.println("-- 저장 시 오류 발생: " + error.getMessage()),
                () -> System.out.println("fetchRealTimePrice + checkLikeMarket 완료"));
    }

    public Mono<Void> checkLikeMarket() {
        return likeMarketRepository.findByIsActiveTrue().flatMap(likeMarket -> {
            Long marketId = likeMarket.getMarketId();
            return marketRepository.findByMarketId(marketId).flatMap(market -> {
                String marketCode = market.getMarketCode();
                String marketKrName = market.getKoreanName();
                String marketEnName = market.getEnglishName();

                return realTimePriceRepository.findRecentPrices(marketId).collectList().filter(
                        prices -> prices.size() >= 2).flatMap(prices -> {
                    RealTimePriceEntity latest = prices.get(0);
                    RealTimePriceEntity previous = prices.get(1);
                    double currentPrice = latest.getTradePrice();
                    double previousPrice = previous.getTradePrice();
                    double changeRate = Math.abs((currentPrice - previousPrice) / previousPrice);

                    if (currentPrice != previousPrice) {
                        String message = String.format(
                                "📢 관심 종목 가격 변동 : marketId %s | %s | %s | %s | 이전: %.10f -> 현재: %.10f (%.10f%%)",
                                marketId, marketCode, marketKrName, marketEnName, previousPrice,
                                currentPrice, changeRate * 100);

                        Long userId = likeMarket.getUserId();
                        return userRepository.findById(userId).flatMap(userEntity -> {
                            Sinks.Many<ServerSentEvent<String>> sink = userSinkMap.get(
                                    userEntity.getUuid());
                            if (sink != null) {
                                sink.tryEmitNext(ServerSentEvent.builder(message).build());
                            }

                            return saveNotification(userId, marketId, message);
                        });
                    }
                    return Mono.empty();
                });
            });
        }).then();
    }

    private Mono<Void> saveNotification(Long userId, Long marketId, String message) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(userId);
        notification.setMarketId(marketId);
        notification.setLog(message);

        return notificationRepository.save(notification).then();
    }

    private Mono<String> callUpbitApiWithMarkets(String markets) {
        try {
            String serverUrl = "https://api.upbit.com/v1/ticker?markets=" + markets;

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(serverUrl)).GET().build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Mono.just(response.body());
            } else {
                System.err.println("API 응답 에러: " + response.statusCode());
                return Mono.error(new RuntimeException("API 호출 실패: " + response.statusCode()));
            }
        } catch (Exception e) {
            System.err.println("오류 " + e.getMessage());
            e.printStackTrace();
            return Mono.error(new RuntimeException("API 호출 중 예외 발생", e));
        }
    }
}
