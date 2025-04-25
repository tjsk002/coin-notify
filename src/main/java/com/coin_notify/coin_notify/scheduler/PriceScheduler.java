package com.coin_notify.coin_notify.scheduler;

import com.coin_notify.coin_notify.data.entity.RealTimePriceEntity;
import com.coin_notify.coin_notify.data.repository.CoinRepository;
import com.coin_notify.coin_notify.data.repository.RealTimePriceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
	@Autowired
	private CoinRepository coinRepository;

	@Autowired
	private RealTimePriceRepository realTimePriceRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	@Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
	public void run(String... args) throws Exception {
		fetchRealTimePrice();
	}

	public void fetchRealTimePrice() {
		coinRepository.findAllMarketNames()
				.collectList()
				.filter(markets -> !markets.isEmpty())
				.map(markets -> String.join(",", markets))
				.flatMap(this::callUpbitApiWithMarkets)
				.flatMapMany(response -> {
					try {
						JsonNode root = objectMapper.readTree(response);

						return Flux.fromIterable(root)
								.flatMap(node -> {
									String market = node.get("market").asText();
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

									return coinRepository.findByMarket(market)
											.map(coin -> {
												RealTimePriceEntity price = new RealTimePriceEntity();
												price.setCoinId(coin.getId());
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
				.subscribe(saved -> System.out.println("-- 저장됨: "));

	}

	private Mono<String> callUpbitApiWithMarkets(String markets) {
		try {
			String serverUrl = "https://api.upbit.com/v1/ticker?markets=" + markets;

			HttpClient client = HttpClient.newHttpClient();
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create(serverUrl))
					.GET()
					.build();

			HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

			if (response.statusCode() == 200) {
				return Mono.just(response.body());
			} else {
				System.err.println("API 응답 에러: " + response.statusCode());
				return Mono.error(new RuntimeException("API 호출 실패: " + response.statusCode()));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Mono.error(new RuntimeException("API 호출 중 예외 발생", e));
		}
	}
}
