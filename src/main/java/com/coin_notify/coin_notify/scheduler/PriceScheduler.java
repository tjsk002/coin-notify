package com.coin_notify.coin_notify.scheduler;

import com.coin_notify.coin_notify.data.entity.RealTimePriceEntity;
import com.coin_notify.coin_notify.data.repository.LikeMarketRepository;
import com.coin_notify.coin_notify.data.repository.MarketRepository;
import com.coin_notify.coin_notify.data.repository.RealTimePriceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class PriceScheduler implements CommandLineRunner {
	private final MarketRepository marketRepository;
	private final LikeMarketRepository likeMarketRepository;
	private final RealTimePriceRepository realTimePriceRepository;
	private final ObjectMapper objectMapper;

	public PriceScheduler(MarketRepository marketRepository, LikeMarketRepository likeMarketRepository, RealTimePriceRepository realTimePriceRepository, ObjectMapper objectMapper) {
		this.marketRepository = marketRepository;
		this.likeMarketRepository = likeMarketRepository;
		this.realTimePriceRepository = realTimePriceRepository;
		this.objectMapper = objectMapper;
	}

	@Override
	public void run(String... args) throws Exception {
		fetchRealTimePrice();
	}

	@Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
	public void fetchRealTimePrice() {
		marketRepository.findAllMarketCodes()
				.collectList()
				.filter(markets -> !markets.isEmpty())
				.map(markets -> String.join(",",
						markets))
				.flatMap(this::callUpbitApiWithMarkets)
				.flatMapMany(response -> {
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
						e.printStackTrace();
						return Flux.empty();
					}
				})
				.flatMap(realTimePriceRepository::save)
				.then(checkLikeMarket())
				.subscribe(unused -> {},
						error -> System.err.println("에러 발생: " + error.getMessage()),
						() -> System.out.println("fetchRealTimePrice + checkLikeMarket 완료"));
	}

	public Mono<Void> checkLikeMarket() {
		return likeMarketRepository.findAll().flatMap(likeMarket -> {
			Long marketId = likeMarket.getMarketId();
			return marketRepository.findByMarketId(marketId).flatMap(market -> {
				String marketCode = market.getMarketCode();
				String marketKrName = market.getKoreanName();
				String marketEnName = market.getEnglishName();

				return realTimePriceRepository.findRecentPrices(marketId)
						.collectList()
						.filter(prices -> prices.size() >= 2)
						.flatMap(prices -> {
							RealTimePriceEntity latest = prices.get(0);
							RealTimePriceEntity previous = prices.get(1);
							double currentPrice = latest.getTradePrice();
							double previousPrice = previous.getTradePrice();
							double changeRate = Math.abs((currentPrice - previousPrice) / previousPrice);

							if (changeRate >= 0.000000001) {
								System.out.printf("📢 관심 종목 가격 변동 (0.000000001%% 이상): marketId %s | %s | %s | %s | 이전: %.2f -> 현재: %.2f (%.10f%%)%n",
										marketId,
										marketCode,
										marketKrName,
										marketEnName,
										previousPrice,
										currentPrice,
										changeRate * 100);
							}

							return Mono.empty();
						});
			});
		}).then();
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
			e.printStackTrace();
			return Mono.error(new RuntimeException("API 호출 중 예외 발생",
					e));
		}
	}
}
