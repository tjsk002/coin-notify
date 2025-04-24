package com.coin_notify.coin_notify.service;

import com.coin_notify.coin_notify.data.entity.CoinEntity;
import com.coin_notify.coin_notify.data.repository.CoinRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class CoinService {
	private final ObjectMapper objectMapper;
	private final HttpClient client;
	private final CoinRepository coinRepository;

	public CoinService(ObjectMapper objectMapper, CoinRepository coinRepository) {
		this.objectMapper = objectMapper;
		this.coinRepository = coinRepository;
		this.client = HttpClient.newHttpClient();
	}

	public Mono<Void> coinCollectionProcess() {
		return fetchMarketData().flatMap(this::saveMarketData);
	}

	private Mono<JsonNode> fetchMarketData() {
		String url = "https://api.upbit.com/v1/market/all?is_details=true";

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).header("accept", "application/json").build();

		return Mono.fromCallable(() -> {
			try {
				HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
				return objectMapper.readTree(response.body());
			} catch (Exception e) {
				throw new RuntimeException("Failed to fetch market data", e);
			}
		});
	}

	private Mono<Void> saveMarketData(JsonNode marketData) {
		return Flux.fromIterable(marketData).map(coin -> {
			CoinEntity coinEntity = new CoinEntity();
			coinEntity.setMarket(coin.get("market").asText());
			coinEntity.setKoreanName(coin.get("korean_name").asText());
			coinEntity.setEnglishName(coin.get("english_name").asText());
			System.out.println(coin.get("market_event"));
			return coinEntity;
		}).flatMap(coinRepository::save).doOnNext(saved -> System.out.println("Saved: " + saved.getMarket())).doOnError(error -> System.err.println("Error saving: " + error.getMessage())).then();
	}
}
